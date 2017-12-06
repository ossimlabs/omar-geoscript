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
import java.text.SimpleDateFormat
import java.text.DateFormat

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

/*    URL buildQueryUrl(String wmsStartDate, String wmsEndDate, String esUrl) {
         esUrl = https://logging-es.logging.svc.cluster.local:9200/project.omar-dev
        String urlSearchParam = "_search?"
        String timeStampFormat = "yyyy-MM-dd hh:mm:ss.ms"
        String timeStampFormat = "yyyy-MM-dd hh:mm:ss.ms"
        String query = """{"query":{"range":{"timestamp":{"gte":"${wmsStartDate}","lte":"${wmsEndDate}","format":"${timeStampFormat}"}},"term":{"kubernetes.labels.deploymentconfig":"omar-wms-app"}}}"""

        String query = """{"query":{"range":{"@timestamp":{"gte":"2017-11-30T05:48:42.809770+00:00","lte": "2017-11-30T18:06:38.779477+00:00"}}}}"""
        String query = """%7b"query":%7b"bool":%7b"must":%5b%7b"range":%7b"@timestamp":%7b"gt":"2017-11-28T14:04:21+0000"%2c"lt":"2017-11-30T14:04:21+0000"%7d%7d%7d%5d%2c"must_not":%5b%5d%2c"should":%5b%5d%7d%7d%2c"from":0%2c"size":10%2c"sort":%5b%5d%2c%7d"""

        (esUrl + urlSearchParam +  URLEncoder.encode(query, "UTF-8")).toURL()
        (esUrl + urlSearchParam +  query ).toURL()
    }*/

    Layer getLayer(WmsRequest wmsRequest, String req) {

        def days = 1

        Workspace workspace = new Memory()
        Schema schema = new Schema("heatmap", [
                new Field("geom", "Point", wmsRequest.srs)
        ])
        Layer layer = workspace.create(schema)
        log.info "wmsRequest.start_date" + wmsRequest.start_date
        log.info "wmsRequest.end_date" + wmsRequest.end_date

//        URL url = buildQueryUrl(wmsRequest.start_date, wmsRequest.end_date, req)
        URL url = new URL(req);
        log.info("ES Search URL: " + url)


        HttpURLConnection conn = (HttpURLConnection) url.openConnection()
        InputStream is = conn.getInputStream()

        InputStreamReader isr = new InputStreamReader(is)
        BufferedReader br = new BufferedReader(isr)
        def result = new JsonSlurper().parse(br)
        log.info "result" + result
        Projection targetProjection = new Projection(wmsRequest.srs)
        br.close()


        def projectionMap = [:]
        layer.withWriter { writer ->
            for (Integer i = 0; i < result.hits.hits.size(); i++) {
                if ((isValidJson(result.hits.hits.getAt(i)._source.message))) {
                    Feature feature = writer.newFeature
                    Map<String, Object> logmap = new ObjectMapper().readValue(result.hits.hits.getAt(i)._source.message, HashMap.class);

                    String timestamplog = logmap.timestamp
                    log.info "timestamp" + timestamplog
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.ms", Locale.ENGLISH);
                    Date date = format.parse(timestamplog);
                    log.info "date" + date

                    def currenttime = new Date()

                    timediff = Math.abs(currenttime.getTime() - date.getTime())
                    log.info "currtime" + currenttime.getTime()
                    log.info "logtime" + date.getTime()
                    log.info "timediff" + timediff

                    if(timediff <= (days*60*60*24*1000)) {
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
