package omar.geoscript

import groovy.xml.StreamingMarkupBuilder

import geoscript.geom.Point
import geoscript.proj.DecimalDegrees

// import grails.gorm.transactions.Transactional
//
// @Transactional
class GeorssService
{
  def grailsLinkGenerator
  def geoscriptService
  def groovyPageRenderer
  def grailsApplication

  def renderFeed(def params)
  {
    def (workspace, layer) = geoscriptService.getWorkspaceAndLayer('omar:raster_entry')
    def enablePolygon = params.boolean( "enablePolygon" ) ?: false
    def cc = params.cc
    def be = params.be
    def filter

    if ( cc ) {
      filter = "country_code='${cc}'"
    } else if ( be ) {
      filter = "be_number like '%${be}%'"
    } else if ( params.filter ) {
      filter = params.filter
    }

    def rssNode = {
      mkp.xmlDeclaration()
      mkp.declareNamespace( content: "http://purl.org/rss/modules/content/" )
      mkp.declareNamespace( georss: "http://www.georss.org/georss" )

      rss( version: "2.0" ) {
        channel {
          title( "OMAR GeoRSS Feed" )
          link( grailsLinkGenerator.link( controller: 'georss', action: 'index', absolute: true ) )
          description( "Track the newest images added to OMAR" )
          layer.eachFeature(
            max: params.max ?: 100,
            sort: [['acquisition_date', 'desc']],
            filter: filter
          ) { entry ->
            def geom = entry.ground_geom

            item {
              title( "${entry.acquisition_date ?: ""} ${entry.country_code ?:  ""} ${entry.target_id?:""} ${entry.image_id?: entry.filename}" )

              def serverBaseURL = grailsApplication.config.grails.serverURL - grailsApplication.config.grails.server.contextPath
              def bounds = entry.ground_geom.bounds

              def tlvParams = [
                bbox: "${bounds.minX},${bounds.minY},${bounds.maxX},${bounds.maxY}",
                filter: "in(${entry.attributes.id})",
                location: [geom.centroid.x, geom.centroid.y].join(',')
              ]

              def tlvURL = grailsLinkGenerator.link( base: serverBaseURL, uri: '/tlv', params: tlvParams, absolute: true )
              def superOverlayURL = grailsLinkGenerator.link(base: serverBaseURL, uri: "/omar-superoverlay/createKml/${entry.attributes.id}", absolute: true)

              link( tlvURL )
              // Using point because polygon is not supported by ESRI ArcGIS Explorer or OpenLayers
              // The code below will support polygons when everyone else does
              // The polygons array is for adding Multi-polygons in the future.
              if ( !enablePolygon )
              {
                def centroid = geom.centroid //entry.groundGeom.centroid

                'georss:point'( "${centroid.y} ${centroid.x}" )
              }
              else
              {
                def pts = geom.coordinates.collect { "${it.y} ${it.x}" }.join( ' ' )

                'georss:polygon'( pts )
              }

              def (minLonDMS, minLatDMS) = dd2dms(bounds.minX, bounds.minY)
              def (maxLonDMS, maxLatDMS) = dd2dms(bounds.maxX, bounds.minY)

              def content = groovyPageRenderer.render( template: '/georss/metadata', model: [
                      tlvURL: tlvURL,
                      superOverlayURL: superOverlayURL,
                      entry: entry,
                      properties: [
                       image_id: 'Image ID',
                       mission_id: 'Mission ID',
                       security_classification: 'Security Class',
                       niirs: 'NIIRS',
                       country_code: 'Country Code',
                       be_number: 'BE Number',
                       acquisition_date: 'Acquisition Date',
                       width: 'Width',
                       height: 'Height',
                      ],
                      minLonDMS: minLonDMS,
                      maxLonDMS: maxLonDMS,
                      minLatDMS: minLatDMS,
                      maxLatDMS: maxLatDMS
              ] )
              'content:encoded' { mkp.yieldUnescaped( "<![CDATA[${content}]]>" ) }
            }
          }
        }
      }
    }


    def rssBuilder = new StreamingMarkupBuilder()
    def rssWriter = new StringWriter()

    rssWriter << rssBuilder.bind( rssNode )
    workspace.close()

    [contentType: 'application/rss+xml"', text: rssWriter.toString()]
  }

  def dd2dms(def ddLon, def ddLat)
  {
    def pt = new DecimalDegrees( new Point(ddLon, ddLat) )
    pt.toDms().split(',')*.trim()
  }
}
