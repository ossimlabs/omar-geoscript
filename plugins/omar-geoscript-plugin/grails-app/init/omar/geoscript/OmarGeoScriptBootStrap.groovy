package omar.geoscript

import org.geotools.factory.Hints
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryCollection
import geoscript.GeoScript
import grails.converters.JSON
import groovy.json.JsonSlurper

import geoscript.feature.Feature


import geoscript.filter.Function

/**
 * Created by sbortman on 1/4/16.
 */
class OmarGeoScriptBootStrap
{
  def dataSourceService

  def init = { servletContext ->
    Hints.putSystemDefault( Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE )
    dataSourceService.readFromConfig()

     JSON.registerObjectMarshaller(Geometry) {
      def json = GeoScript.wrap(it).geoJSON
   
      new JsonSlurper().parseText(json)
    }
   
      JSON.registerObjectMarshaller(GeometryCollection) {
       def json = GeoScript.wrap(it).geoJSON
   
       new JsonSlurper().parseText(json)
     }

    JSON.registerObjectMarshaller(Feature) {
      new JsonSlurper().parseText(it.geoJSON)
    }

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
  }

  def destroy = {
  }
}
