package geoscript.app

import mil.nga.giat.data.elasticsearch.*

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.http.HttpHost;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.NameImpl;
import org.geotools.util.logging.Logging;
import org.opengis.feature.type.Name;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import mil.nga.giat.data.elasticsearch.ElasticAttribute.ElasticGeometryType;
import mil.nga.giat.shaded.es.common.joda.Joda;


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

        try {
            println "got before rest client"
            final RestClient restClient = RestClient.builder(new HttpHost(searchHost, hostPort, "http")).build();
            println "got after rest client" + restClient.toString()
            final Response response = restClient.performRequest("GET", "/", Collections.<String, String>emptyMap());
            println "restClient" + restClient.toString()
            println "code" + response.getStatusLine().getStatusCode()
            if (response.getStatusLine().getStatusCode() >= 400) {
                throw new IOException();
            }
            client = new RestElasticClient(restClient);
        } catch (Exception e) {
            throw new IOException("Unable to create REST client", e);
        }
    }
}