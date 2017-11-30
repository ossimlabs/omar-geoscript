package omar.geoscript

import grails.validation.Validateable
import groovy.transform.ToString

/**
 * Created by sbortman on 11/24/14.
 */

@ToString( includeNames = true )
class WmsRequest implements Validateable
{
  String service
  String version
  String request
  String srs
  String bbox
  Integer width
  Integer height
  String format
  String layers
  String styles
  Boolean transparent
  String start_date
  String end_date
  // Double max_gsd
}