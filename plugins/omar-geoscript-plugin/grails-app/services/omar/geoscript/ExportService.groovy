package omar.geoscript

import geoscript.filter.Filter
import geoscript.layer.Layer
import geoscript.workspace.PostGIS
import geoscript.workspace.Directory

import java.nio.file.Files

class ExportService {
    ZipService zipService

    def exportShapeZip(def params) {

        PostGIS postgis = new PostGIS('omardb-prod', user: 'postgres')
        File directory = Files.createTempDirectory("shape-").toFile()
        Directory shapeDir = new Directory(directory)

        Layer srcLayer = postgis['raster_entry']
        Layer destLayer = shapeDir.create(srcLayer.schema)

        println params 
        
        srcLayer.eachFeature(
            filter: params?.filter ?: Filter.PASS,
            // sort: [],
            max: 1000,
            start: params?.start ?: 0
        ) { f ->
           destLayer.add( f )
        }

        postgis?.close()
        shapeDir?.close()

        def ant = new AntBuilder()
        File zipFile = new File(directory.parentFile, "${directory.name}.zip")

        //println zipFile

        ant.zip(destfile: "${zipFile.absolutePath}", basedir: directory, includes: '*')

        byte[] buffer = zipFile.bytes

        directory.delete()
        zipFile
    }
}