package omar.geoscript

import geoscript.geom.Bounds
import geoscript.filter.Color
import geoscript.filter.Filter
import geoscript.render.Map as GeoScriptMap
import geoscript.style.Composite
import geoscript.workspace.Workspace
import static geoscript.style.Symbolizers.*

import grails.converters.JSON
import grails.transaction.Transactional

import omar.core.ISO8601DateParser

import javax.imageio.ImageIO

import org.springframework.util.FastByteArrayOutputStream

import  groovy.transform.Memoized
@Transactional(readOnly=true)
class FootprintService
{
  def grailsApplication
  def geoscriptService

  static final int INITIAL_SIZE = 8196
  static final TransparentGif transparentGif = new TransparentGif()

  @Memoized
  private def createStyle(String styleName) 
  {
    // Retrieve the styles for this configuration
    def styles = grailsApplication.config.wms.styles
    // Store all the style map's keys as a list
    def styleKeys = styles.keySet() as List

      // Attempt to retrieve the requested style from our styles map
      def outlineLookupTable = styles[styleName]

      // If the requested style doesn't exist in this map, then use the first style
      // we do have. A size of zero, indicates there are no elements to this style.
      if (outlineLookupTable.size() == 0) {
        println "WARNING: Style '${styleName}' does not exist on this instance. " +
           "Defaulting to first available style '${styleKeys.first()}'."
        outlineLookupTable = styles[styleKeys.first()]
      }

      def style = outlineLookupTable.collect { k, v ->
        ( stroke( color: new Color( v.color ) ) + fill( opacity: 0.0 ) ).where( v.filter )
      }

      def x = outlineLookupTable.keySet().collect { "'${it}'" }.join( ',' )

      // Add the negation of all filters so that things that don't match still show up
      def allFilters = outlineLookupTable.values().collect { it.filter }?.join(' or ')

      style << ( stroke( color: '#000000' ) + fill( opacity: 0.0 ) ).where( "not (${allFilters})" )

      style as Composite
  }

  def getFootprints(GetFootprintsRequest params)
  {
     byte[] buffer = []
    def (prefix, layerName) = params.layers.split( ':' )

    def layerInfo = LayerInfo.where {
      name == layerName && workspaceInfo.namespaceInfo.prefix == prefix
    }.get()

    Workspace.withWorkspace( geoscriptService.getWorkspace( layerInfo.workspaceInfo.workspaceParams ) ) { workspace ->

      def style = createStyle(params.styles)

      def footprints = new QueryLayer( workspace[layerName], style )
      def viewBbox = new Bounds( *( params.bbox.split( ',' )*.toDouble() ), params.srs )
      def geomField = workspace[layerName].schema.geom
      def queryBbox

      if ( !workspace[layerName]?.proj?.equals( viewBbox?.proj ) )
      {
        queryBbox = viewBbox.reproject( workspace[layerName]?.proj )
      }
      else
      {
        queryBbox = viewBbox
      }

      def filter = Filter.intersects( geomField.name, queryBbox.geometry )

      if ( params.filter )
      {
        filter = filter.and( new Filter( params.filter ) )
      }

      // HACK - need to refactor this
      if ( params.time ) {
        def timePairs = ISO8601DateParser.parseOgcTimeStartEndPairs(params.time)

        timePairs?.each { timePair ->
          if ( timePair.start ) {
              filter = filter.and("acquisition_date >= ${timePair.start}")
          }
          if ( timePair.end ) {
              filter = filter.and("acquisition_date <= ${timePair.end}")
          }
        }
      }

      def defaultMaxFeatures = grailsApplication.config.wms.footprints.defaultMaxFeatures

      def options = [
        maxFeatures: params.maxFeatures ?: defaultMaxFeatures ?: Integer.MAX_VALUE
      ]

      footprints.setFilter( filter, options )

      def map = new GeoScriptMap(
          width: params.width,
          height: params.height,
          type: params.format.split( '/' ).last(),
          bounds: viewBbox,
          proj: viewBbox.proj,
          layers: [footprints]
      )

      if ( map?.type?.equalsIgnoreCase('gif') ) {
        map.@renderers['gif'] = transparentGif
      }

      def image = map.renderToImage()

      map.close()

      def ostream = new FastByteArrayOutputStream(INITIAL_SIZE)

      ImageIO.write(image, map.type, ostream)
      buffer = ostream.toByteArrayUnsafe()
    }

    [contentType: params.format, buffer: buffer]
  }

  def getFootprintsLegend(def params)
  {
    def styles = grailsApplication.config.wms.styles
    def outlineLookupTable = styles[params.style]
    def styleKeys = styles.keySet() as List

    // If the requested style doesn't exist in this map, then use the first style
    // we do have. A size of zero, indicates there are no elements to this style.
    if (outlineLookupTable.size() == 0) {
      println "WARNING: Style '${params.styles}' does not exist on this instance. " +
         "Defaulting to first available style '${styleKeys.first()}'."
      outlineLookupTable = styles[styleKeys.first()]
    }

    def style = outlineLookupTable.collect { k, v -> [
      label: k,
      color: new Color( v.color ).hex
    ] }

    (style as JSON)?.toString()
  }
}
