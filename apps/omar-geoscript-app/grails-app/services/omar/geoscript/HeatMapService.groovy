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

    def processHeatmap(String key, String trust, String req) {

        int i, count
        ObjectMapper mapper = new ObjectMapper();
        Memory mem = new Memory()

        Map<String, Object> map = new HashMap<String, Object>();

        KeyStore keyStore = KeyStore.getInstance("JKS"); // or "PKCS12"
        FileInputStream instream = new FileInputStream(new File(key));

        keyStore.load(instream,  "kspass".toCharArray());

        KeyStore trustStore = KeyStore.getInstance("JKS")
        FileInputStream instreamtks = new FileInputStream(new File(trust));
        trustStore.load(instreamtks, "tspass".toCharArray())

        SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(trustStore, null)
                    .loadKeyMaterial(keyStore, "kspass".toCharArray()) // use null as second param if you don't have a separate key password
                    .build();

        HttpClient httpClient = HttpClients.custom().setSSLContext(sslContext).build();
        HttpResponse response = httpClient.execute(new HttpGet(req));
        HttpEntity entity = response.getEntity()


        System.out.println("response Status ");
            System.out.println(response.getStatusLine());
           System.out.println("entity" + entity);

        BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
        def result = new JsonSlurper().parse(br)

        count = 0
        for(i = 0;i<result.hits.hits.size();i++)
        {
            if((isValidJson(result.hits.hits.getAt(i)._source.message))) {
                println result.hits.hits.getAt(i)._source.message
                count++
                println "\ncount" + count
                // parse message....pass count, bbox, filename to datastore

                // convert JSON string to Map
                map = mapper.readValue(result.hits.hits.getAt(i)._source.message, new TypeReference<Map<String, String>>(){});

                System.out.println(map.get("bbox"));
                System.out.println(map.get("filename"));
                System.out.println(map.get("timestamp"));


            }


        }


        EntityUtils.consume(entity);

    }
}