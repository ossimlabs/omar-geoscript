package omar.geoscript

import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation

@Api(value = "/export",
     description = "Export Support"
)

class ExportController {
    ExportService exportService
    static int DEFAULT_MAX_FEATURES = 1000

    @ApiOperation(value = "Export shapefile from the server",
                produces='application/zip',
                httpMethod="GET",
                nickname = "exportShapefile")
    @ApiImplicitParams([
          @ApiImplicitParam(name = 'typeName', value = 'Type name', defaultValue="omar:raster_entry", paramType = 'query', dataType = 'string', required=true),
          @ApiImplicitParam(name = 'filter', value = 'Filter', paramType = 'query', dataType = 'string', required=false),
          @ApiImplicitParam(name = 'maxFeatures', value = 'Maximum Features in the results', paramType = 'query', defaultValue="10", dataType = 'integer', required=false),
          @ApiImplicitParam(name = 'startIndex', value = 'Starting offset', defaultValue="0", paramType = 'query', dataType = 'integer', required=false),
    ])
    def exportShapefile(String typeName, String filter, Integer maxFeatures, Integer startIndex) {       

        if (!maxFeatures) {
            maxFeatures = DEFAULT_MAX_FEATURES
        }

        def results = exportService.exportShapeZip(typeName,  filter,  maxFeatures,  startIndex)
        response.setContentType("application/zip")
        response.setHeader("Content-disposition", "filename=${results.name}")
        response.outputStream << results.bytes
        results.delete()
    }

}