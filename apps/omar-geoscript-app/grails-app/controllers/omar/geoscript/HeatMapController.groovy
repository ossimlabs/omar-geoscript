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
		BindUtil.fixParamNames( WmsRequest, params )
	   bindData( wmsRequest, params )

		// error checking code
		def bbox_int = new int[4]
		def process = 1

		arrayList = wms.bbox.split(",")
		arrayList.length
		if(arrayList.length != 4)
		{
			log.error "bbox does not have valid numbers"
			process = 0
		}

		for(int i = 0;i<4;i++)
			bbox_int[i] = Integer.valueOf(arrayList[i])

		if(bbox_int[0] < -180 || bbox_int[3] > 180 || bbox_int[1] < -90 || bbox_int[3] > 90) {
			log.error "bbox out of bounds"
            process = 0
		}

		if((bbox_int[2] <= bbox_int[0]) || (bbox_int[3] <= bbox_int[1])) {
			log.error "bbox range is wrong"
            process = 0
		}


		if(process == 1) {
			def results = heatMapService.getTile(wmsRequest, elasticSearchURL)

			render contentType: results.contentType, file: results.buffer
		}
	}
}