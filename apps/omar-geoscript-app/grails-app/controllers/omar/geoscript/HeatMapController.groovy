package omar.geoscript

class HeatMapController {

    def heatMapService

    def index () {}

    def getTile() {
        render heatMapService.getTile("https://logging-es.logging.svc.cluster.local:9200/project.omar-dev.ebadd419-70ba-11e7-a545-0e704fd9c8b2.2017.11.07/_search?q=requestMethod&q=GetMap&pretty&size=10000&filter_path=hits.hits._source.message")
    }
}