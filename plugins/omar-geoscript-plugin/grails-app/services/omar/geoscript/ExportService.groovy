package omar.geoscript

import geoscript.filter.Filter
import geoscript.layer.Layer
import geoscript.workspace.PostGIS
import geoscript.workspace.Directory

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import geoscript.GeoScript

class ExportService {
    ZipService zipService

    def exportShapeZip(def params) {

// [typeName:omar:raster_entry, maxFeatures:25, controller:export, format:null, action:exportShapefile]

        def options = parseOptions(params)
        PostGIS postgis = new PostGIS('omardb-prod', user: 'postgres')
        File directory = Files.createTempDirectory("shape-").toFile()
        Directory shapeDir = new Directory(directory)
        if ( !options.containsKey("max") || !params.maxFeatures ) {
            options.max = 100
        }
        def (prefix, layerName) = params?.typeName?params.typeName.split(':'):[null,null]
        println options
        // println params?.maxFeatures ?: 0
        Layer srcLayer = postgis[layerName]
        Layer destLayer = shapeDir.create(srcLayer.schema)

        println "exportshapezip params = ${params}"
        // def mx = '10'
        // srcLayer.eachFeature(
        //     // filter: params?.filter ?: Filter.PASS,
        //     // sort: [],
        //     max: params.maxFeatures
        //     // start: params?.start ?: 0
        // ) { f ->
        //    destLayer.add( f )
        // }
        srcLayer.collectFromFeature(options) { f ->
        destLayer.add(f)
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

  def exportShape(def inputLayer, def options)
  {
    this.zipService = zipService
    log.info "exporting shapefile Geoscript"
    Path scratchDir = Paths.get('/Users/kaseykemmerer/Projects/geoscript-scratch')
    def outputDirectory = Files.createTempDirectory( scratchDir, 'shape-')
    String stringTempDir = outputDirectory.toString()

    String outZip = 'shapefile_export'
    Directory shapeDir = new Directory( stringTempDir )
    HashMap result = [:]
    def outputLayer = shapeDir.create(inputLayer.schema)
    Integer count = 0;
    inputLayer.collectFromFeature(options) { f ->
      ++count
      outputLayer.add(f)
    }

    log.info "Shape file outputLayer = ${outputLayer} \n\n"
    result.count = count

    //CREATE A ZIP OF SHAPEFILE

    File zipFile = new File("${stringTempDir}/${outZip}.zip")
    zipService.run(stringTempDir, 'shapefile_export')

    byte[] buffer = zipFile.bytes
    // result.data = buffer
    result.data = zipFile
    // result;
    zipFile
  }

  def parseOptions(def wfsParams)
  {
    log.info "geoscript service wfsParams = ${wfsParams}"
    def wfsParamNames = [
        'maxFeatures', 'startIndex', 'sortBy', 'filter'
    ]

    def options = wfsParamNames.inject( [:] ) { options, wfsParamName ->
      if ( wfsParams[wfsParamName] != null )
      {
        switch ( wfsParamName )
        {
        case 'maxFeatures':
          options['max'] = wfsParams[wfsParamName].toInteger()
          break
        case 'startIndex':
          options['start'] = wfsParams[wfsParamName].toInteger()
          break
        // case 'propertyName':
        //   def fields = wfsParams[wfsParamName]?.split( ',' )?.collect {
        //     it.split( ':' )?.last()
        //   } as List<String>
        //   if ( fields && !fields?.isEmpty() && fields?.every { it } )
        //   {
        //     options['fields'] = fields
        //   }
        //   break
        case 'sortBy':
          if ( wfsParams[wfsParamName]?.trim() )
          {
            options['sort'] = wfsParams[wfsParamName].split( ',' )?.collect {
              def props = [it] as List
              if ( it.contains( " " ) )
              {
                props = it.split( ' ' ) as List
              }
              else if ( it.contains( "+" ) )
              {
                props = it.split( "\\+" ) as List
              }

              if ( props?.size() == 2 )
              {
                props[1] = ( props[1].equalsIgnoreCase( 'D' ) ) ? 'DESC' : 'ASC'
              }
              props
            }
          }
          break
        default:
          if ( wfsParams[wfsParamName] )
          {
            options[wfsParamName] = wfsParams[wfsParamName]
          }
        }
      }
      options
    }

    options
  }
    
}