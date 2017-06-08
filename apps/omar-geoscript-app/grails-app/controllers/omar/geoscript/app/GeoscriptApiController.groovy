package omar.geoscript.app

import grails.converters.JSON

class GeoscriptApiController
{
  def geoscriptService

  def getCapabilitiesData()
  {
    render geoscriptService.capabilitiesData as JSON
  }

  def getSchemaInfoByTypeName()
  {
    render geoscriptService.getSchemaInfoByTypeName(params?.typeName) as JSON
  }

  def getFeatureGML3()
  {
    render geoscriptService.getFeatureGML3(params)
  }

}
