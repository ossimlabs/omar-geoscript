package omar.geoscript

import org.geotools.factory.Hints
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryCollection
import geoscript.GeoScript
import grails.converters.JSON
import groovy.json.JsonSlurper

import geoscript.feature.Feature
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
  }

  def destroy = {
  }
}
