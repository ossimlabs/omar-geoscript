package omar.geoscript

import org.springframework.beans.factory.annotation.Value
import omar.geoscript.WmsRequest

class HeatMapController {

	def heatMapService

	@Value('${geoscript.elasticsearch.url}')
	def elasticSearchURL

	def index () {}

	def getTile(WmsRequest wmsRequest)
	{
	//println params
	//println wmsRequest

		def results = heatMapService.getTile( wmsRequest, elasticSearchURL )

		render contentType: results.contentType, file: results.buffer
	}
}