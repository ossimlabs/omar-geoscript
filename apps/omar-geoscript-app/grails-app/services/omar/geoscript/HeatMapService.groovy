package omar.geoscript


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext
import java.security.KeyStore

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import groovy.json.JsonSlurper;
import java.io.BufferedReader
import java.io.InputStreamReader
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import com.fasterxml.jackson.core.type.TypeReference;


import org.springframework.beans.factory.annotation.Value
import geoscript.geom.*
import geoscript.feature.*
import geoscript.layer.Layer
import geoscript.layer.io.GeoJSONReader
import geoscript.workspace.*
import geoscript.process.Process as GeoScriptProcess
import geoscript.style.ColorMap
import geoscript.render.Map as GeoScriptMap
import geoscript.proj.Projection
import java.text.SimpleDateFormat
import java.text.DateFormat
import java.util.Date


class HeatMapService {

    private Boolean isValidJson(String maybeJson){
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(maybeJson);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    URL buildQueryUrl(String wmsStartDate, String wmsEndDate, String esUrl) {
      // esUrl = https://logging-es.logging.svc.cluster.local:9200/project.omar-dev*/
        String urlSearchParam = "_search?"
        String timeStampFormat = "yyyy-MM-dd hh:mm:ss.ms"
        String query = """{
            "query": {
                "range" : {
                    "timestamp" : {
                        "gte": "${wmsStartDate}",
                        "lte": "${wmsEndDate}",
                        "format": "${timeStampFormat}"
                    }
                },
                "term": { "kubernetes.labels.deploymentconfig": "omar-wms-app" }
            }
        }"""

        new URL(esUrl+urlSearchParam+query)

    }

    Layer getLayer(WmsRequest wmsRequest, String req)
    {
        Workspace workspace = new Memory()
        Schema schema = new Schema("heatmap", [
            new Field("geom","Point",wmsRequest.srs)
        ])
        Layer layer = workspace.create(schema)
        log.info "wmsRequest.start_date" + wmsRequest.start_date
        log.info "wmsRequest.end_date" + wmsRequest.end_date
        log.info "req" + req



        // append start date and end date to url before opening
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.ms", Locale.ENGLISH);
//        Date startdate = format.parse(wmsRequest.start_date);
//        log.info "startdate" + startdate

        // append start date and end date to url before opening
//        Date enddate = format.parse(wmsRequest.end_date);
//        log.info "enddate" + enddate

        URL url = buildQueryUrl(wmsRequest.start_date, wmsRequest.end_date, req)
        HttpURLConnection conn = (HttpURLConnection) url.openConnection()
        InputStream is = conn.getInputStream()

        InputStreamReader isr = new InputStreamReader(is)
        BufferedReader br = new BufferedReader(isr)
        def result = new JsonSlurper().parse(br)
        Projection targetProjection = new Projection(wmsRequest.srs)
        br.close()


        def projectionMap = [:]
        layer.withWriter{ writer ->
            for(Integer i = 0;i<result.hits.hits.size();i++)
            {
                if((isValidJson(result.hits.hits.getAt(i)._source.message))) {
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
        GeoScriptProcess proc = new GeoScriptProcess( "vec:Heatmap" )
        Projection targetProjection = new Projection(wmsRequest.srs)
        Bounds bounds = wmsRequest.bbox.split( "," )*.toDouble() as Bounds
        bounds.proj = new Projection(layer.proj)
        def raster = proc.execute(
                data: layer,
                radiusPixels: 20,
                pixelsPerCell: 1,
                outputBBOX: bounds.env,
                outputWidth: wmsRequest.width,
                outputHeight: wmsRequest.height
        )?.result
        raster.style = new ColorMap( [
            [color: "#FFFFFF", quantity: 0, label: "nodata", opacity: 0],
            [color: "#FFFFFF", quantity: 0.02, label: "nodata", opacity: 0],
            [color: "#4444FF", quantity: 0.1, label: "nodata"],
            [color: "#FF0000", quantity: 0.5, label: "values"],
            [color: "#FFFF00", quantity: 1.0, label: "values"]
        ] ).opacity( 0.25 )

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
        map.render( buffer )
        map.close()

        [contentType: wmsRequest.format, buffer: buffer.toByteArray()]
    }


}
