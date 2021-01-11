package omar.geoscript

import geoscript.filter.Filter
import geoscript.layer.Layer
import geoscript.workspace.Directory
import geoscript.workspace.Workspace
import org.geotools.data.DataStoreFinder
import java.nio.file.Files
import geoscript.GeoScript

class ExportService {
  
    def exportShapeZip(String typeName, String filter, Integer maxFeatures, Integer startIndex) {

        File directory = Files.createTempDirectory("shape-").toFile()
        Directory shapeDir = new Directory(directory)

        def (prefix, layerName) = typeName?typeName.split(':'):[null,null]
        Layer srcLayer  = findLayer(prefix, layerName)
        Layer destLayer = shapeDir.create(srcLayer.schema)

        srcLayer.eachFeature(
            filter: filter ?: Filter.PASS,
            max: maxFeatures.toInteger(),
            start: startIndex?.toInteger() ?: 0
        ) { f ->
           destLayer.add( f )
        }

        shapeDir?.close()

        def ant = new AntBuilder()
        File zipFile = new File(directory.parentFile, "${directory.name}.zip")

        ant.zip(destfile: "${zipFile.absolutePath}", basedir: directory, includes: '*')

        byte[] buffer = zipFile.bytes

        directory.delete()
        zipFile
    }


  Workspace getWorkspace(Map params) {
    def dataStore = DataStoreFinder.getDataStore( params )

    ( dataStore ) ? GeoScript.wrap( dataStore ) : null
  }

  def findLayer(String prefix, String layerName) {

    if(!prefix&!layerName)
    {
      return null
    }

    def layerInfo = LayerInfo.where {
        name == layerName && workspaceInfo.namespaceInfo.prefix == prefix
    }.get()

    def workspaceParams = layerInfo?.workspaceInfo?.workspaceParams
    def workspace = getWorkspace(workspaceParams)

    workspace[layerName]
  }
}