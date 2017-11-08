package omar.geoscript


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
//import org.apache.http.ssl.TrustStrategy
//import org.apache.http.ssl.TrustSelfSignedStrategy
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


class HeatMapService {

/*    @Value('${geoscript.elasticsearch.host}')
    String host

    @Value('${geoscript.elasticsearch.keystorefile}')
    String keystorepass

    @Value('${geoscript.elasticsearch.keystorepass}')
    String keystoreloc

    @Value('${geoscript.elasticsearch.port}')
    Integer port

    @Value('${geoscript.elasticsearch.index}')
    String index

    @Value('${geoscript.elasticsearch.search}')
    String searchIndices */

    private Boolean isValidJson(String maybeJson){
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(maybeJson);
            return true;
        } catch (IOException e) {
            return false;
        }
    }


    Layer getLayer(String req) 
    {
        Workspace workspace = new Memory()
        Schema schema = new Schema("heatmap", [
            new Field("geom","Point","EPSG:4326"),
            new Field("value","String"),
            new Field("class","String"),
            new Field("type","String")
        ])
        Layer layer = workspace.create(schema)

        Integer count = 0;
        URL url = new URL(req);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        InputStream is = conn.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        def result = new JsonSlurper().parse(br)

        br.close();

        layer.withWriter{ writer ->
            for(Integer i = 0;i<result.hits.hits.size();i++)
            {
                if((isValidJson(result.hits.hits.getAt(i)._source.message))) {
                    Feature feature = writer.newFeature
                    Map<String, Object> map = new ObjectMapper().readValue(result.hits.hits.getAt(i)._source.message, HashMap.class);

                    def minx = map.get("bbox").minX
                    def miny = map.get("bbox").minY
                    feature.set([
                        
                        geom: new Point(minx, miny)
                        
                        ])
                    writer.add(feature)

                }
            }
        }
        layer
    }


/*
    def processHeatmap(String key, String trust, String req) {

        int i, count
        ObjectMapper mapper = new ObjectMapper();
//        Memory mem = new Memory()

//        KeyStore keyStore = KeyStore.getInstance("JKS"); // or "PKCS12"
//        FileInputStream instream = new FileInputStream(new File(key));

//        keyStore.load(instream,  "kspass".toCharArray());

//        KeyStore trustStore = KeyStore.getInstance("JKS")
//        FileInputStream instreamtks = new FileInputStream(new File(trust));
//        trustStore.load(instreamtks, "tspass".toCharArray())

//        SSLContext sslContext = SSLContexts.custom()
//                    .loadTrustMaterial(trustStore, null)
//                    .loadKeyMaterial(keyStore, "kspass".toCharArray()) // use null as second param if you don't have a separate key password
//                    .build();

//        HttpClient httpClient = HttpClients.custom().setSSLContext(sslContext).build();
        HttpClient httpClient = HttpClients.createDefault()
        HttpResponse response = httpClient.execute(new HttpGet(req));
        HttpEntity entity = response.getEntity()

        BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
        def result = new JsonSlurper().parse(br)

        count = 0
        for(i = 0;i<result.hits.hits.size();i++)
        {
            if((isValidJson(result.hits.hits.getAt(i)._source.message))) {
                println result.hits.hits.getAt(i)._source.message
                count++
                println "\ncount" + count
                Map<String, Object> map = new ObjectMapper().readValue(result.hits.hits.getAt(i)._source.message, HashMap.class);

                System.out.println(map.get("bbox"));
                System.out.println(map.get("filename"));
                System.out.println(map.get("timestamp"));


            }


        }


        EntityUtils.consume(entity);

    }
*/
}