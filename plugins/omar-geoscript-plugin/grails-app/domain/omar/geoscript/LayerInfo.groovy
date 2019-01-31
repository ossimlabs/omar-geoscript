package omar.geoscript

import groovy.transform.ToString

@ToString(includeNames = true, excludes = 'workspaceInfo')
class LayerInfo
{
  String name
  String title
  String description
  String[] keywords
  String query
  String geomName
  String geomType
  String geomSRS

  static belongsTo = [workspaceInfo: WorkspaceInfo]

  static mapping = {
      cache true
      id generator: 'identity'
  }

  static constraints = {
    name()
    title()
    description()
    keywords()
    query(nullable: true)
    geomName(nullable: true)
    geomType(nullable: true)
    geomSRS(nullable: true)
  }
}
