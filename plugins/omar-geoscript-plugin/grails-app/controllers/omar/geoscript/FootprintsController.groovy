package omar.geoscript

import grails.async.web.AsyncController

class FootprintsController implements AsyncController
{
  def footprintService

  def getFootprints(GetFootprintsRequest cmd)
  {
    def ctx = startAsync()
    ctx.start {
      def results = footprintService.getFootprints( cmd )

      render contentType: results.contentType, file: results.buffer
      ctx.complete()
    }
  }

  def getFootprintsLegend(def params)
  {
    render contentType: 'application/json',
      text:  footprintService.getFootprintsLegend(params)
  }
}
