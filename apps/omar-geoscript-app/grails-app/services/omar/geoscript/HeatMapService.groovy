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

    def processHeatmap(String key, String trust, String req) {

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
        println "result" + result
        println "result hits" + result.hits
        //def hitsarray = result.get("hits")
        //println "hitsarray" + hitsarray


        /* String line;
        while ((line = br.readLine())!= null) {
            System.out.println(line);
        } */


        EntityUtils.consume(entity);

    }
}