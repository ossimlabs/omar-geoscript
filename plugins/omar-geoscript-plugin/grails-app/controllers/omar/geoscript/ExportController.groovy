package omar.geoscript

class ExportController {
    ExportService exportService

    def exportShapefile() {       
        def results = exportService.exportShapeZip(params)
        response.setContentType("application/zip")
        response.setHeader("Content-disposition", "filename=${results.name}")
        response.outputStream << results.bytes
        results.delete()
    }

}