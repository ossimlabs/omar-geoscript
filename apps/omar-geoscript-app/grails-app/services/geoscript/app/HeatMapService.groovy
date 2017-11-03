package geoscript.app

//import mil.nga.giat.data.elasticsearch.*


//import org.apache.http.HttpHost;
//import org.elasticsearch.client.Response;
//import org.elasticsearch.client.RestClient;
import static org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import javax.net.ssl.SSLContext
import java.io.InputStream
import java.security.KeyStore



import org.springframework.beans.factory.annotation.Value

class HeatMapService {

    /*@Value('${geoscript.elasticsearch.host}')
    String searchHost

    @Value('${geoscript.elasticsearch.port}')
    Integer hostPort

    @Value('${geoscript.elasticsearch.index}')
    String indexName

    @Value('${geoscript.elasticsearch.search}')
    String searchIndices*/

    def processHeatmap() {

        def String KEYSTOREPASS = "keystorepass";

        KeyStore keyStore = KeyStore.getInstance("JKS"); // or "PKCS12"
        FileInputStream instream = new FileInputStream(new File("/home/omar/es/admin.jks"));
        println "before keystore load"

        keyStore.load(instream, null);

        println "keystore" + keyStore

        SSLContext sslContext = SSLContexts.custom()
                    .loadKeyMaterial(keyStore, null) // use null as second param if you don't have a separate key password
                    .build();

            HttpClient httpClient = HttpClients.custom().setSSLContext(sslContext).build();
            HttpResponse response = httpClient.execute(new HttpGet("https://logging-es.logging.svc.cluster.local:9200/.all/_search?pretty"));
             HttpEntity entity = response.getEntity();

            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());
            EntityUtils.consume(entity);
        }
       }

        /*try {
            println "got before rest client"
            final RestClient restClient = RestClient.builder(new HttpHost(searchHost, hostPort, "http")).build();
            println "got after rest client"
            final Response response = restClient.performRequest("GET", "/", Collections.<String, String>emptyMap());
            println "restClient" + restClient.toString()
            println "code" + response.getStatusLine().getStatusCode()
            if (response.getStatusLine().getStatusCode() >= 400) {
                throw new IOException();
            }
            client = new RestElasticClient(restClient);
        } catch (Exception e) {
            throw new IOException("Unable to create REST client", e);
        } */