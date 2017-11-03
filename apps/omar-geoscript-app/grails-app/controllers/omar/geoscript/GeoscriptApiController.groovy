package omar.geoscript

import grails.converters.JSON

import io.swagger.annotations.*

@Api(value = "/geoscriptApi",
     description = "GeoScript Support"
)
class GeoscriptApiController
{
  def geoscriptService

  @ApiOperation(value = "Get the capabilities of the server", produces='application/json')
  def getCapabilitiesData()
  {
    println params
    render geoscriptService.capabilitiesData as JSON
  }

  @ApiOperation(value = "List available projections", produces='application/json')
  def listProjections()
  {
    println params
    render geoscriptService.listProjections() as JSON
  }

  @ApiOperation(value = "Get the schema of a given type", produces='application/json')
  @ApiImplicitParams([
    @ApiImplicitParam(name = 'typeName', value = 'Type Name', defaultValue="omar:raster_entry", paramType = 'query', dataType = 'string', required=true)
  ])
  def getSchemaInfoByTypeName()
  {
    println params
    render geoscriptService.getSchemaInfoByTypeName(params?.typeName) as JSON
  }

  @ApiOperation(value = "Query a layer",
    produces='application/json',
        notes = """
*   **sort** Is a comma separated field list of the form  
    <tab><field> <sort></tab>  

    <tab>**Examples:**</tab>  
    <tab><tab>**acquisition_date DESC**</tab></tab>****  
    <tab><tab>**acquisition_date ASC**</tab></tab>****  
    <tab><tab>**acquisition_date DESC,width ASC**</tab></tab>********
""")
  @ApiImplicitParams([
    @ApiImplicitParam(name = 'typeName', value = 'Type name', defaultValue="omar:raster_entry", paramType = 'query', dataType = 'string', required=true),
    @ApiImplicitParam(name = 'filter', value = 'Filter', paramType = 'query', dataType = 'string', required=false),
    @ApiImplicitParam(name = 'resultType', value = 'Result type', defaultValue="results", allowableValues="[results,hits]", paramType = 'query', dataType = 'string', required=false),
    @ApiImplicitParam(name = 'featureFormat', value = 'Feature Output format', defaultValue="JSON", allowableValues="[JSON, KML, CSV, GML2, GML3, GML32]", paramType = 'query', dataType = 'string', required=false),
    @ApiImplicitParam(name = 'sort', value = 'Sort by', paramType = 'query', dataType = 'string'),
    @ApiImplicitParam(name = 'fields', value = 'Field names (comma separated fields)', defaultValue="", paramType = 'query', dataType = 'string', required=false),
    @ApiImplicitParam(name = 'max', value = 'Maximum Features in the result', defaultValue="10", paramType = 'query', dataType = 'int', required=false),
    @ApiImplicitParam(name = 'start', value = 'Starting offset', defaultValue="0", paramType = 'query', dataType = 'int', required=false),
  ])
  def queryLayer()
  {
    def options = params?.inject([:]) { a, b ->
      if ( b?.value ) {
        switch(b.key)
        {
        case 'max':
          a['max'] = b?.value?.toInteger()
          break
        case 'sort':
          a['sort'] = b?.value?.split(',')?.collect { it?.split(' ') as List } as List
          break
        case 'filter':
          a['filter'] = b?.value
          break
        case 'start':
          a['start'] = b?.value?.toInteger()
          break
        case 'fields':
          a['fields'] = b?.value?.split(',')?.collect { it.trim() } as List<String>
          break
        }
      }
      a
    }

    render geoscriptService.queryLayer(
      params?.typeName,
      options,
      params?.resultType ?: 'results',
      params?.featureFormat
    ) as JSON

    // render geoscriptService.queryLayer(params) as JSON
  }

}
