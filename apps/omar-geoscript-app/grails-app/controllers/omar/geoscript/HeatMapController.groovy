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

		try {
			if (wmsRequest.validate()) {
				results = heatMapService.getTile(wmsRequest, elasticSearchURL)
			} else {
				HashMap ogcExceptionResult = OgcExceptionUtil.formatWmsException(wmsRequest)
				println "got to else"
				results.contentType = ogcExceptionResult.contentType
				println "got after contenttype"
				results.buffer = ogcExceptionResult.buffer
				println "got after buffer"
				// response.contentLength = ogcExceptionResult.buffer.length
			}
		}

		catch ( e )
		{
			log.error(e.toString())
		}

		render contentType: results.contentType, file: results.buffer
	}
}