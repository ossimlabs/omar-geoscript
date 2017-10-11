package geoscript.app

import mil.nga.giat.data.elasticsearch.*

import org.springframework.beans.factory.annotation.Value


class ElasticSearchService {

    String searchHost
    Integer hostPort
    String indexName
    String searchIndices

    ElasticDataStore  ES = new ElasticDataStore(searchHost, hostPort, indexName, searchIndices)

}