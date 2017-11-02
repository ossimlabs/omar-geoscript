package geoscript.app

import mil.nga.giat.data.elasticsearch.*

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

        ElasticDataStore ES = new ElasticDataStore(searchHost, hostPort, indexName, searchIndices)
    }

}