package geoscript.app

import mil.nga.giat.data.elasticsearch.*

import org.springframework.beans.factory.annotation.Value

class HeatMapService {

    @Value('${geoscript.elasticsearch.host}')
    String searchHost

    @Value('${geoscript.elasticsearch.port}')
    Integer hostPort

    @Value('${geoscript.elasticsearch.index}')
    String indexName

    @Value('${geoscript.elasticsearch.search}')
    String searchIndices

    def processHeatmap() {

        def firsttime = 1

        // call to our elastic search

        if (firsttime == 1) {
            ElasticDataStore ES = new ElasticDataStore(searchHost, hostPort, indexName, searchIndices)
            firsttime = 0
        }

         

    }

}