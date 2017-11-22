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

	public boolean isInteger(String string) {
		try {
			Integer.valueOf(string);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	def getTile(WmsRequest wmsRequest)
	{
		println "params" + params
		println "wmsrequest" + wmsRequest
		BindUtil.fixParamNames( WmsRequest, params )
        bindData( wmsRequest, params )
		def results

		try {
			if (wmsRequest.validate()) {
				results = heatMapService.getTile(wmsRequest, elasticSearchURL)
			} else {
				HashMap ogcExceptionResult = OgcExceptionUtil.formatWmsException(wmsRequest)
				results.contentType = ogcExceptionResult.contentType
				results.buffer = ogcExceptionResult.buffer
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