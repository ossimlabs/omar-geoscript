package omar.geoscript

import com.fasterxml.jackson.databind.ObjectMapper
import geoscript.feature.*
import geoscript.geom.*
import geoscript.layer.Layer
import geoscript.process.Process as GeoScriptProcess
import geoscript.proj.Projection
import geoscript.render.Map as GeoScriptMap
import geoscript.style.ColorMap
import geoscript.workspace.*
import groovy.json.JsonSlurper
import java.net.URLEncoder

class HeatMapService {

    private Boolean isValidJson(String maybeJson) {
        try {
            final ObjectMapper mapper = new ObjectMapper()
            mapper.readTree(maybeJson)
            return true
        } catch (IOException e) {
            return false
        }
    }

    URL buildQueryUrl(String wmsStartDate, String wmsEndDate, String esUrl) {
        // esUrl = https://logging-es.logging.svc.cluster.local:9200/project.omar-dev*/
        String urlSearchParam = "_search?"
        String timeStampFormat = "yyyy-MM-dd hh:mm:ss.ms"
        String query = """{"query":{"range":{"timestamp":{"gte":"${wmsStartDate}","lte":"${wmsEndDate}","format":"${timeStampFormat}"}},"term":{"kubernetes.labels.deploymentconfig":"omar-wms-app"}}}"""


        (esUrl + urlSearchParam +  URLEncoder.encode(query, "UTF-8")).toURL()

    }

    Layer getLayer(WmsRequest wmsRequest, String req) {
        Workspace workspace = new Memory()
        Schema schema = new Schema("heatmap", [
                new Field("geom", "Point", wmsRequest.srs)
        ])
        Layer layer = workspace.create(schema)
        log.info "wmsRequest.start_date" + wmsRequest.start_date
        log.info "wmsRequest.end_date" + wmsRequest.end_date
        log.info "req" + req


        URL url = buildQueryUrl(wmsRequest.start_date, wmsRequest.end_date, req)
        log.info("ES Search URL: " + url)

        HttpURLConnection conn = (HttpURLConnection) url.openConnection()
        InputStream is = conn.getInputStream()

        InputStreamReader isr = new InputStreamReader(is)
        BufferedReader br = new BufferedReader(isr)
        def result = new JsonSlurper().parse(br)
        Projection targetProjection = new Projection(wmsRequest.srs)
        br.close()


        def projectionMap = [:]
        layer.withWriter { writer ->
            for (Integer i = 0; i < result.hits.hits.size(); i++) {
                if ((isValidJson(result.hits.hits.getAt(i)._source.message))) {
                    Feature feature = writer.newFeature
                    Map<String, Object> logmap = new ObjectMapper().readValue(result.hits.hits.getAt(i)._source.message, HashMap.class);


                    Point centroid = new Point((logmap.bbox.minX +
                            logmap.bbox.maxX) / 2.0,
                            (logmap.bbox.minY +
                                    logmap.bbox.maxY) / 2.0)


                    Projection proj = projectionMap."${logmap.bbox.proj.id}"
                    if (!proj) {
                        proj = new Projection(logmap.bbox.proj.id)
                        projectionMap."${logmap.bbox.proj.id}" = proj
                    }
                    Point targetPoint = proj.transform(centroid, targetProjection) as Point


                    feature.set([
                            geom: targetPoint
                    ])
                    writer.add(feature)
                }
            }
        }
        layer.proj = targetProjection

        layer
    }


    def getTile(WmsRequest wmsRequest, String elasticURL) {
        Layer layer = getLayer(wmsRequest, elasticURL)
        GeoScriptProcess proc = new GeoScriptProcess("vec:Heatmap")
        Projection targetProjection = new Projection(wmsRequest.srs)
        Bounds bounds = wmsRequest.bbox.split(",")*.toDouble() as Bounds
        bounds.proj = new Projection(layer.proj)
        def raster = proc.execute(
                data: layer,
                radiusPixels: 20,
                pixelsPerCell: 1,
                outputBBOX: bounds.env,
                outputWidth: wmsRequest.width,
                outputHeight: wmsRequest.height
        )?.result
        raster.style = new ColorMap([
                [color: "#FFFFFF", quantity: 0, label: "nodata", opacity: 0],
                [color: "#FFFFFF", quantity: 0.02, label: "nodata", opacity: 0],
                [color: "#4444FF", quantity: 0.1, label: "nodata"],
                [color: "#FF0000", quantity: 0.5, label: "values"],
                [color: "#FFFF00", quantity: 1.0, label: "values"]
        ]).opacity(0.25)

        def map = new GeoScriptMap(
                width: wmsRequest.width,
                height: wmsRequest.height,
                type: wmsRequest.format.split("/")[-1],
                proj: targetProjection,
                bounds: bounds,
                layers: [
                        raster
                ]
        )

        def buffer = new ByteArrayOutputStream()
        map.render(buffer)
        map.close()

        [contentType: wmsRequest.format, buffer: buffer.toByteArray()]
    }
}
