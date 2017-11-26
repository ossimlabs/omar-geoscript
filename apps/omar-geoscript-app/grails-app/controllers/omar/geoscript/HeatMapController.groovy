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
		BindUtil.fixParamNames( WmsRequest, params )
        bindData( wmsRequest, params )
		def results

		try {
			if (wmsRequest.validate()) {
				results = heatMapService.getTile(wmsRequest, elasticSearchURL)
			} else {
				log.error "Make sure all fields of the request are non-null"
//				HashMap ogcExceptionResult = OgcExceptionUtil.formatWmsException(wmsRequest)
//				results.contentType = ogcExceptionResult.contentType
//				results.buffer = ogcExceptionResult.buffer
			}
		}

		catch ( e )
		{
			log.error(e.toString())
		}

		if(results != null)
			render contentType: results.contentType, file: results.buffer

	}
}
