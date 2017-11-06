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

    def processHeatmap(String req) {

        KeyStore keyStore = KeyStore.getInstance("JKS"); // or "PKCS12"
        FileInputStream instream = new FileInputStream(new File("/home/omar/es/key"));
        //  String req = "https://" + host + ":" + port + "/" + index + "/" + searchIndices
        keyStore.load(instream,  "kspass".toCharArray());

        KeyStore trustStore = KeyStore.getInstance("JKS")
        FileInputStream instreamtks = new FileInputStream(new File("/home/omar/es/truststore"));
        trustStore.load(instreamtks, "tspass".toCharArray())

        SSLContext sslContext = SSLContexts.custom()
//                    .loadTrustMaterial(trustStore)
                    .loadKeyMaterial(keyStore, "kspass".toCharArray()) // use null as second param if you don't have a separate key password
                    .build();

        // Allow TLSv1 protocol only
/*        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslContext);
        CloseableHttpClient httpclient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .build();

        HttpGet httpget = new HttpGet(req);

        System.out.println("Executing request " + httpget.getRequestLine());

        CloseableHttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();

            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());
            EntityUtils.consume(entity); */


            HttpClient httpClient = HttpClients.custom().setSSLContext(sslContext).build();
        println "req" + req
        HttpResponse response = httpClient.execute(new HttpGet(req));
        HttpEntity entity = response.getEntity();

            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());
            EntityUtils.consume(entity);
        }
       }