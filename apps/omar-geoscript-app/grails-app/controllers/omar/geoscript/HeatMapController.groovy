package omar.geoscript

import org.springframework.beans.factory.annotation.Value
import omar.geoscript.WmsRequest
import omar.core.BindUtil

class HeatMapController {

	def heatMapService

	@Value('${geoscript.elasticsearch.url}')
	def elasticSearchURL

	def index () {}

	def getTile(WmsRequest wmsRequest)
	{
		println params
		println wmsRequest
		BindUtil.fixParamNames( WmsRequest, params )
	   bindData( wmsRequest, params )

		def results = heatMapService.getTile( wmsRequest, elasticSearchURL )

		render contentType: results.contentType, file: results.buffer
	}
}