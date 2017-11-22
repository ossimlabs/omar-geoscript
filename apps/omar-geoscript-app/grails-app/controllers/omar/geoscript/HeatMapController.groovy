package omar.geoscript

import org.springframework.beans.factory.annotation.Value
import omar.geoscript.WmsRequest
import omar.core.BindUtil
import omar.core.OgcExceptionUtil


class HeatMapController {

	def heatMapService

	@Value('${geoscript.elasticsearch.url}')
	def elasticSearchURL

	def index () {}

	def getTile(WmsRequest wmsRequest)
	{
		println "params" + params
		println "wmsrequest" + wmsRequest
		BindUtil.fixParamNames( WmsRequest, params )
        bindData( wmsRequest, params )
		println "params after bind" + params
		println "wmsrequest after bind" + wmsRequest
		def results

		println "validate" + wmsRequest.validate()

		try {
//			if (wmsRequest.validate()) {
//				println "got to if"
				results = heatMapService.getTile(wmsRequest, elasticSearchURL)
//				println "results" + results
//			} else {
//				println "got to else"
//				HashMap ogcExceptionResult = OgcExceptionUtil.formatWmsException(wmsRequest)
//				results.contentType = ogcExceptionResult.contentType
//				results.buffer = ogcExceptionResult.buffer
				// response.contentLength = ogcExceptionResult.buffer.length
//			}
		}

		catch ( e )
		{
			log.error(e.toString())
		}

		render contentType: results.contentType, file: results.buffer
	}
}