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
		println "params" + params
		println "wmsrequest" + wmsRequest

		println "got before fixParamNames"
		BindUtil.fixParamNames( WmsRequest, params )
		println "got before bindData"
	   bindData( wmsRequest, params )

		// error checking code

		println "got before get tile"
		def results = heatMapService.getTile( wmsRequest, elasticSearchURL )

		render contentType: results.contentType, file: results.buffer
	}
}