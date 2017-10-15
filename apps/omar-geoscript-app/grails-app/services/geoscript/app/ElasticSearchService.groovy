package geoscript.app

import mil.nga.giat.data.elasticsearch.*

import org.springframework.beans.factory.annotation.Value


class ElasticSearchService {

    @Value('${geoscript.elasticsearch.host}')
    String searchHost

    @Value('${geoscript.elasticsearch.port}')
    Integer hostPort

    @Value('${geoscript.elasticsearch.index}')
    String indexName

    @Value('${geoscript.elasticsearch.search}')
    String searchIndices

    ElasticDataStore  ES = new ElasticDataStore(searchHost, hostPort, indexName, searchIndices)

}