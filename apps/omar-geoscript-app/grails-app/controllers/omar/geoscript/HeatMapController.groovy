package omar.geoscript

import org.springframework.beans.factory.annotation.Value


class HeatMapController {

    def heatMapService

    @Value('${geoscript.elasticsearch.url}')
    def elasticSearchURL

    def index () {}

    def getTile() {
        render heatMapService.getTile(elasticSearchURL)
    }
}