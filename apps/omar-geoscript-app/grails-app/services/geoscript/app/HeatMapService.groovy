package geoscript.app

//import mil.nga.giat.data.elasticsearch.*


//import org.apache.http.HttpHost;
//import org.elasticsearch.client.Response;
//import org.elasticsearch.client.RestClient;


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

    def processHeatmap(String searchHost, Integer hostPort,
                       String indexName, String searchIndices ) {

        println "searchhsort" + searchHost
        println "hostport" + hostPort
        println "indexname" + indexName
        println "searchindicies" + searchIndices
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
    }
}