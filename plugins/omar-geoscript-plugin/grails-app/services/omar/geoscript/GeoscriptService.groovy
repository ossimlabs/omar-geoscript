package omar.geoscript

import geoscript.GeoScript
import geoscript.feature.Field
import geoscript.filter.Filter
import geoscript.filter.Function
import geoscript.geom.Bounds
import geoscript.geom.GeometryCollection
import geoscript.layer.Layer
import geoscript.layer.io.CsvWriter
import geoscript.proj.Projection
import geoscript.workspace.Database
import geoscript.workspace.Memory
import geoscript.workspace.Workspace
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.Memoized
import groovy.xml.StreamingMarkupBuilder
import omar.core.DateUtil
import org.geotools.data.DataStoreFinder
import org.geotools.factory.CommonFactoryFinder
import org.geotools.referencing.CRS
import org.opengis.filter.capability.FunctionName

import org.springframework.beans.factory.InitializingBean


import grails.gorm.transactions.Transactional

import java.time.Instant


import org.springframework.beans.factory.annotation.Value

@Transactional( readOnly = true )
class GeoscriptService implements InitializingBean
{
  def grailsLinkGenerator
  def grailsApplication
  def jsonSlurper = new JsonSlurper()

  @Value('${geoscript.defaultMaxFeatures}')
  Integer defaultMaxFeatures
  @Value('${geoscript.downloadURL}')
  String downloadURL
  @Value('${geoscript.downloadRootDir}')
  String downloadRootDir
  @Value('${geoscript.downloadMissions}')
  List<String> downloadMissions

  def parseOptions(def wfsParams)
  {
    def wfsParamNames = [
        'maxFeatures', 'startIndex', 'propertyName', 'sortBy', 'filter'
    ]

    def options = wfsParamNames.inject( [:] ) { options, wfsParamName ->
      if ( wfsParams[wfsParamName] != null )
      {
        switch ( wfsParamName )
        {
        case 'maxFeatures':
          options['max'] = wfsParams[wfsParamName]
          break
        case 'startIndex':
          options['start'] = wfsParams[wfsParamName]
          break
        case 'propertyName':
          def fields = wfsParams[wfsParamName]?.split( ',' )?.collect {
            it.split( ':' )?.last()
          } as List<String>
          if ( fields && !fields?.isEmpty() && fields?.every { it } )
          {
            options['fields'] = fields
          }
          break
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

  def findLayerInfo(def wfsParams)
  {
    def x = wfsParams?.typeName?.split( ':' )
    def namespacePrefix
    def layerName

    switch ( x?.size() )
    {
    case 1:
      layerName = x?.last()
      break
    case 2:
      (namespacePrefix, layerName) = x
      break
    }

    def namespaceInfo

    if ( !namespacePrefix && wfsParams?.namespace )
    {
      def pattern = /xmlns\(\w+=(.*)\)/
      def matcher = wfsParams?.namespace =~ pattern

      if ( matcher )
      {
        def uri = matcher[0][1]

        namespaceInfo = NamespaceInfo.findByUri( uri )
      }
      else
      {
        log.info "${'*' * 20} No Match ${'*' * 20}"
      }

      layerName = wfsParams?.typeName?.split( ':' )?.last()
    }
    else
    {
      namespaceInfo = NamespaceInfo.findByPrefix( namespacePrefix )
    }

    LayerInfo.where {
      name == layerName && workspaceInfo.namespaceInfo == namespaceInfo
    }.get()
  }

  def getWorkspaceAndLayer(String layerName)
  {
    def layerInfo = findLayerInfo( [typeName: layerName] )
    def workspace = getWorkspace( layerInfo?.workspaceInfo?.workspaceParams )

    def layer = getLayerFromInfo(layerInfo, workspace)

    [workspace, layer]
  }

  Layer getLayerFromInfo(LayerInfo layerInfo, Workspace workspace)
  {
    Layer layer

    try
    {
      if (layerInfo.query && layerInfo.geomName && layerInfo.geomType && layerInfo.geomSrs ) {
          Database database = new Database( workspace.ds )
          layer = database.createView(layerInfo.name, layerInfo.query,
            new Field( layerInfo.geomName, layerInfo.geomType, layerInfo.geomSrs ) )
        } else {
          layer = workspace[layerInfo?.name]
        }
    }
    catch ( Exception e )
    {
      throw new Exception("Can't instantiate layer: ${layerInfo}")
    }

    return layer
  }

  @Memoized
  def listFunctions2()
  {
    def start = System.currentTimeMillis()

    List names = []
    CommonFactoryFinder.getFunctionFactories().each { f ->
      f.functionNames.each { fn ->
        if ( fn instanceof FunctionName )
        {
          names << [name: fn.functionName.toString(), argCount: fn.argumentCount]
        }
      }
    }

    def stop = System.currentTimeMillis()

    log.info ( [name: 'listFunctions2', time: stop - start] as String )

    names.sort { a, b -> a.name.compareToIgnoreCase b.name }
  }
/*
  @Override
  void afterPropertiesSet() throws Exception
  {

//    Thread.start {
      Function.registerFunction( "queryCollection" ) { String layerName, String attributeName, String filter ->
        def (workspace, layer) = getWorkspaceAndLayer( layerName )
        def results = layer?.collectFromFeature( filter ) { it[attributeName] }
        workspace?.close()
        results
      }

      Function.registerFunction( 'collectGeometries' ) { def geometries ->
        def multiType = ( geometries ) ? "geoscript.geom.Multi${geometries[0].class.simpleName}" : new GeometryCollection( geometries )

        Class.forName( multiType ).newInstance( geometries )
      }
//    }

    defaultMaxFeatures = grailsApplication.config.geoscript.defaultMaxFeatures as Integer
    downloadURL = grailsApplication.config.geoscript.downloadURL
    downloadRootDir = grailsApplication.config.geoscript.downloadRootDir
  }
  */

  Workspace getWorkspace(Map params)
  {
    def dataStore = DataStoreFinder.getDataStore( params )

    ( dataStore ) ? GeoScript.wrap( dataStore ) : null
  }

  def getSchemaInfoByTypeName(String typeName)
  {
    def requestType = "GET"
    def requestMethod = "GetSchemaInfoByTypeName"
    Date startTime = new Date()
    def responseTime
    def requestInfoLog

    def (prefix, layerName) = typeName?.split( ':' )

    def layerInfo = LayerInfo.where {
      name == layerName && workspaceInfo.namespaceInfo.prefix == prefix
    }.get()

    def workspaceInfo = layerInfo.workspaceInfo
    def namespaceInfo = workspaceInfo.namespaceInfo

    def schemaInfo = [
        name: layerName,
        namespace: [prefix: namespaceInfo.prefix, uri: namespaceInfo.uri],
        schemaLocation: grailsLinkGenerator.serverBaseURL
    ]

    Workspace.withWorkspace( getWorkspace( workspaceInfo?.workspaceParams ) ) { workspace ->
      def layer =  workspace[layerName] //getLayerFromInfo(layerInfo, workspace)
      def schema = layer.schema

      schemaInfo.attributes = layer.schema.fields.collect { field ->
        def descr = schema.featureType.getDescriptor( field.name )
        [
            maxOccurs: descr.maxOccurs,
            minOccurs: descr.minOccurs,
            name: field.name,
            nillable: descr.nillable,
            type: field.typ
        ]
      }
    }

    Date endTime = new Date()
    responseTime = Math.abs(startTime.getTime() - endTime.getTime())

    requestInfoLog = new JsonBuilder(timestamp: DateUtil.formatUTC(startTime), requestType: requestType,
            requestMethod: requestMethod, endTime: DateUtil.formatUTC(endTime), responseTime: responseTime,
            responseSize: schemaInfo.toString().getBytes().length, typeName: typeName)

    log.info requestInfoLog.toString()

    schemaInfo
  }

  def getCapabilitiesData()
  {
    def requestType = "GET"
    def requestMethod = "GetCapabilitiesData"
    Date startTime = new Date()
    def responseTime
    def requestInfoLog

    def getCapabilities = [
        featureTypes: getLayerData(),
        functionNames: listFunctions2(),
        featureTypeNamespacesByPrefix: NamespaceInfo.list().inject( [:] ) { a, b ->
          a[b.prefix] = b.uri; a
        }
    ]

    Date endTime = new Date()
    responseTime = Math.abs(startTime.getTime() - endTime.getTime())

    requestInfoLog = new JsonBuilder(timestamp: DateUtil.formatUTC(startTime), requestType: requestType,
            requestMethod: requestMethod, endTime: DateUtil.formatUTC(endTime), responseTime: responseTime,
            responseSize: getCapabilities.toString().getBytes().length)

    log.info requestInfoLog.toString()

    getCapabilities
  }

  @Memoized
  def getLayerData()
  {
    def start = System.currentTimeMillis()

    def listOfLayers = LayerInfo.list()?.collect { layerInfo ->
      def layerData
      WorkspaceInfo workspaceInfo = WorkspaceInfo.findByName( layerInfo.workspaceInfo.name )
      Workspace.withWorkspace( getWorkspace( workspaceInfo?.workspaceParams ) ) { Workspace workspace ->
        def layer = workspace[layerInfo.name] // getLayerFromInfo(layerInfo, workspace)
        def uri = layer?.schema?.uri
        def prefix = NamespaceInfo.findByUri( uri )?.prefix
        def geoBounds

        if ( layer.count() > 0 )
        {
          geoBounds = ( layer?.proj?.epsg == 4326 ) ? layer?.bounds : layer?.bounds?.reproject( 'epsg:4326' )
        }
        else
        {
          geoBounds = [minX: -180.0, minY: -90.0, maxX: 180.0, maxY: 90.0]
        }
       layerData = [
            name: layerInfo?.name,
           namespace: [prefix: prefix, uri: uri],
            title: layerInfo?.title,
            description: layerInfo?.description,
            keywords: layerInfo?.keywords,
            proj: layer?.proj?.id,
            geoBounds: [minX: geoBounds?.minX, minY: geoBounds?.minY, maxX: geoBounds?.maxX, maxY: geoBounds?.maxY,]
        ]
      }

      layerData
    }

    def stop = System.currentTimeMillis()

    log.info ( [name: 'getLayerData', time: stop - start] as String )

    listOfLayers
  }

  def getFeatureCsv(def wfsParams)
  {
    def layerInfo = findLayerInfo( wfsParams )
    def result

    def writer = new CsvWriter()
    Workspace.withWorkspace( getWorkspace( layerInfo.workspaceInfo.workspaceParams ) ) {
      workspace ->
        def layer = workspace[layerInfo.name]
        result = writer.write( layer.filter( wfsParams.filter ) )

        workspace.close()
    }


    result
  }

  def getFeatureGML3(def wfsParams)
  {
    def layerInfo = findLayerInfo( wfsParams )
    def xml

    def options = parseOptions( wfsParams )
    def workspaceParams = layerInfo?.workspaceInfo?.workspaceParams

    def x = {

      Workspace.withWorkspace( getWorkspace( workspaceParams ) ) {
        workspace ->
          def layer = workspace[layerInfo.name]
          def matched = layer?.count( wfsParams.filter ?: Filter.PASS )
          def count = ( wfsParams.maxFeatures ) ? Math.min( matched, wfsParams.maxFeatures ) : matched
          def namespaceInfo = layerInfo?.workspaceInfo?.namespaceInfo

          def schemaLocations = [
              namespaceInfo.uri,
              grailsLinkGenerator.link( absolute: true, uri: '/wfs', params: [
                  service: 'WFS',
                  version: wfsParams.version,
                  request: 'DescribeFeatureType',
                  typeName: wfsParams.typeName
              ] ),
              "http://www.opengis.net/wfs",
              grailsLinkGenerator.link( absolute: true, uri: '/schemas/wfs/1.1.0/wfs.xsd' )
          ]

          mkp.xmlDeclaration()
          mkp.declareNamespace( ogcNamespacesByPrefix )
          mkp.declareNamespace( "${namespaceInfo.prefix}": namespaceInfo.uri )

          wfs.FeatureCollection(
              numberOfFeatures: count,
              timeStamp: new Date().format( "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getTimeZone( 'GMT' ) ),
              'xsi:schemaLocation': schemaLocations.join( ' ' ),
              numberMatched: matched,
              startIndex: wfsParams.startIndex ?: '0'
          ) {
            if ( !( wfsParams?.resultType?.toLowerCase() == 'hits' ) )
            {
              def features = layer?.getFeatures( options )

              gml.featureMembers {
                features?.each { feature ->
                  mkp.yieldUnescaped(
                      feature.getGml( version: 3, format: false, bounds: false, xmldecl: false, nsprefix: namespaceInfo.prefix )
                  )
                }
              }
            }
          }
      }
    }

    xml = new StreamingMarkupBuilder( encoding: 'utf-8' ).bind( x )

    return xml.toString()
  }

  def getFeatureJSON(def wfsParams)
  {
    def layerInfo = findLayerInfo( wfsParams )
    def results

    def options = parseOptions( wfsParams )

    Workspace.withWorkspace( getWorkspace( layerInfo.workspaceInfo.workspaceParams ) ) {
      workspace ->
        def layer = workspace[layerInfo.name]
        def count = layer.count( wfsParams.filter ?: Filter.PASS )

        def features = ( wfsParams.resultType == 'hits') ? [] : layer.collectFromFeature( options ) {
          feature -> return new JsonSlurper().parseText( feature.geoJSON )
        }

        results = [
            crs: [
                properties: [
                    name: "urn:ogc:def:crs:${layer.proj.id}"
                ],
                type: "name"
            ],
            features: features,
            totalFeatures: count,
            type: "FeatureCollection"
        ]

        workspace.close()
    }


    return JsonOutput.toJson( results )
  }

  def getFeatureKML(def wfsParams)
  {
    def layerInfo = findLayerInfo( wfsParams )
    def result

    def options = parseOptions( wfsParams )

    Workspace.withWorkspace( getWorkspace( layerInfo.workspaceInfo.workspaceParams ) ) {
      workspace ->
        def layer = workspace[layerInfo.name]
        def features = layer.getFeatures( options )
        result = kmlService.getFeaturesKml( features, [:] )

        workspace.close()
    }


    result
  }

  def queryLayer(String typeName, Map<String,Object> options, String resultType='results', String featureFormat=null, Boolean includeNumberMatched=null)
  {
    def info = [
      name: 'queryLayer',
      typeName: typeName,
      options: options,
      resultType: resultType,
      featureFormat: featureFormat,
      includeNumberMatched: includeNumberMatched
    ]

    log.info( info as String )

      def requestType = "GET"
      def requestMethod = "QueryLayer"
      Date startTime = new Date()
      def responseTime
      def httpStatus
      def requestInfoLog

      def (prefix, layerName) = typeName?typeName.split(':'):[null,null]
      def layer = findLayer(prefix, layerName)
      def results

      if(resultType?.toLowerCase() == "hits")
      {
        includeNumberMatched = true
      }

      if ( options.bbox )
      {
        def bbox = options.bbox
        def bounds = new Bounds(bbox.minX, bbox.minY, bbox.maxX, bbox.maxY, bbox.proj.id)
        def geom = bounds.proj.transform(bounds.geometry, 'epsg:4326')
        def geoBbox = bounds.proj.transform(bounds.geometry, 'epsg:4326')
        def filter = Filter.intersects('ground_geom', geom)

        if ( options.filter ) {
            options.filter =  filter.and( options?.filter )
        } else {
          options.filter = filter
        }
        options.geoBbox = geoBbox
      }

      if(layer)
      {
        Workspace.withWorkspace(layer?.workspace) {
            Long matched
            if(includeNumberMatched)
            {
              matched =  layer?.count( options.filter )
            }
            def features = []
            Long count = 0;
            if ( resultType == 'results' )
            {
                if ( featureFormat == 'CSV' ) {
                  def csvResult = exportCSV(layer, options)
                  features = csvResult?.data
                  count = csvResult.count
                } else {
                  // Clamp number of features returned to prevent
                  // memory/performance issues.  defaultMaxFeatures can be set
                  // via configuration paramters
                  if ( ! options.max || options.max > defaultMaxFeatures ) {
                    options.max = defaultMaxFeatures
                  }

                  log.info '-' * 50
                  log.info options as String
                  log.info '-' * 50

                  Projection srcProj
                  Projection destProj

                  if ( options?.srsName ) {
                    srcProj = new Projection( "epsg:4326" )
                    destProj = new Projection( options?.srsName )
                  }

                  features = layer?.collectFromFeature(options) { feature ->
                    ++count;
                    if ( options?.srsName ) {
                      feature.geom = Projection.transform(feature.geom, srcProj, destProj)
                    }

                    log.info("Download missions is ${downloadMissions}")
                    /* DOWNLOAD HACK - START */
                    if ( downloadURL && downloadRootDir && feature?.mission_id in downloadMissions ) {
                      File imageFile = feature?.filename as File
//                      String downloadLink = "${downloadURL}/${imageFile?.parent - downloadRootDir}"
                      String downloadLink = "<a href='${downloadURL}/${imageFile?.parent - downloadRootDir}/index.html'  target='_blank'>Click to download</a>"

                      Field imageIdField = feature?.schema?.fields?.find { it.name?.toLowerCase() == 'image_id' }

                      if ( imageIdField ) {
                        feature?.set( 'image_id', downloadLink)
                      } else {
                        log.error "No image Id present"
                      }
                    }
                    /* DOWNLOAD HACK - END */

                    formatFeature(feature, featureFormat, [prefix: prefix])
                  }
                }
            }
            else if(includeNumberMatched)
            {
              count = ( options?.max ) ? Math.min( matched, options?.max ) : matched
            }

            results = [
              namespace: [prefix: prefix, uri: layer?.schema?.uri],
              numberOfFeatures: count,
              //numberMatched: matched,
              timeStamp: Instant.now() as String,
              features: features
            ]
            if(includeNumberMatched)
            {
              results.numberMatched = matched
            }
        }
      }

      Date endTime = new Date()
      responseTime = Math.abs(startTime.getTime() - endTime.getTime())

      httpStatus = (results?.features != null) ? 200 : 400

      requestInfoLog = new JsonBuilder(timestamp: DateUtil.formatUTC(startTime), requestType: requestType,
              requestMethod: requestMethod, numberOfFeatures: results?.numberOfFeatures, numberMatched: results?.numberMatched,
              httpStatus: httpStatus, endTime: DateUtil.formatUTC(endTime), responseTime: responseTime,
              responseSize: results.toString().bytes.length, typeName: typeName, options: options?.toString())

      log.info requestInfoLog.toString()

      results
  }

  def exportCSV(def inputLayer, def options)
  {
    HashMap result = [:]
    def memory = new Memory()
    def outputLayer = memory.create(inputLayer.schema)
    Integer count = 0;
    inputLayer.collectFromFeature(options) { f ->
      ++count
      outputLayer.add(f)
    }
    result.count = count
    def writer = new CsvWriter()

    result.data = writer.write(outputLayer)//?.readLines()

    result;
  }

  def findLayer(String prefix, String layerName)
  {
    if(!prefix&!layerName)
    {
      return null
    }

    def layerInfo = LayerInfo.where {
        name == layerName && workspaceInfo.namespaceInfo.prefix == prefix
    }.get()

    def workspaceParams = layerInfo?.workspaceInfo?.workspaceParams
    def workspace = getWorkspace(workspaceParams)

    //getLayerFromInfo(layerInfo, workspace)
    workspace[layerName]
  }

  private def formatFeature(def feature, def featureFormat, def formatParams)
  {
    def version

    if ( featureFormat && featureFormat?.startsWith('GML'))
    {
        switch ( featureFormat )
        {
        case 'GML2':
          version = 2
          break
        case 'GML3':
          version = 3
          break
        case 'GML3_2':
          version = 3.2
          break
        default:
          version = 3
        }

        feature.getGml( version: version, format: false, bounds: false, xmldecl: false, nsprefix: formatParams.prefix )
    }
    else if (featureFormat == 'JSON')
    {
      jsonSlurper.parseText(feature.geoJSON)
      //feature
    }
    else if (featureFormat == 'WMS1_1_1' || featureFormat == "WMS1_3_0")
    {
      jsonSlurper.parseText(feature.geoJSON)
    }
    else
    {
      feature
    }
  }

  @Memoized
  def listProjections()
  {
    def requestType = "GET"
    def requestMethod = "ListProjections"
    Date startTime = new Date()
    def responseTime
    def requestInfoLog
    def httpStatus

/*
    def projs = CRS.getSupportedAuthorities(true).collect { auth ->
        CRS.getSupportedCodes(auth)?.inject([]) { list, code ->
            def id = "${auth}:${code}"
            try
            {
                list << [id: id, units: CRS.decode(id)?.unit?.toString()]
            }
            catch ( e )
            {
              // e.printStackTrace()
            }
            list
        }
    }?.flatten()
*/

    def projs = ProjectionCache.list

    Date endTime = new Date()
    responseTime = Math.abs(startTime.getTime() - endTime.getTime())

    httpStatus = projs != null ? 200 : 400

    requestInfoLog = new JsonBuilder(timestamp: DateUtil.formatUTC(startTime), requestType: requestType,
            requestMethod: requestMethod, endTime: DateUtil.formatUTC(endTime), responseTime: responseTime,
            httpStatus: httpStatus, responseSize: projs.toString().getBytes().length)

    log.info requestInfoLog.toString()
    projs
  }
}
