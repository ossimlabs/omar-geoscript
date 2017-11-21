package omar.geoscript

import org.springframework.beans.factory.annotation.Value
import omar.geoscript.WmsRequest
import omar.core.BindUtil

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

		// error checking bbox
		def bbox_int = new int[4]
		def process = 1

		def arrayList = wmsRequest.bbox.split(",")
		arrayList.length
		if(arrayList.length != 4)
		{
			process = 0
			log.error "bbox does not have valid numbers"
		}

		for(int i = 0;i<4;i++)
			bbox_int[i] = Integer.valueOf(arrayList[i])

		if(bbox_int[0] < -180 || bbox_int[3] > 180 || bbox_int[1] < -90 || bbox_int[3] > 90) {
			process = 0
			log.error "bbox out of bounds"
		}

		if((bbox_int[2] <= bbox_int[0]) || (bbox_int[3] <= bbox_int[1])) {
			process = 0
			log.error "bbox range is wrong"
		}

		// error checking length and width
        boolean width_valid = isInteger(wmsRequest.width)
		if(width_valid == false)
		{
			process = 0
			log.error "width is not valid"
		}

		boolean height_valid = isInteger(wmsRequest.height)
		if(height_valid == false)
		{
			process = 0
			log.error "height is not valid"
		}





		if(process == 1) {
			def results = heatMapService.getTile(wmsRequest, elasticSearchURL)

			render contentType: results.contentType, file: results.buffer
		}
	}
}