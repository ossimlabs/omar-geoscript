package omar.geoscript

import grails.gorm.transactions.Transactional

import java.util.regex.Pattern
import java.util.regex.Matcher


@Transactional
class DataSourceService
{
	def grailsApplication
	def messageSource
	WFSConfig wfsConfig

	static String expandEnvVars(String text) {

		String txt = text

		if(txt)
		{
			String pattern = '\\$\\{([A-Za-z0-9]+)\\}';
			Pattern expr = Pattern.compile(pattern);
			Matcher matcher = expr.matcher(txt);
			while (matcher.find()) {
				String envValue = System.env."${matcher.group(1)}";
				if (envValue == null) {
					envValue = "";
				} else {
					envValue = envValue.replace('\\', '\\\\');
				}
				Pattern subexpr = Pattern.compile(Pattern.quote(matcher.group(0)));
				txt = subexpr.matcher(txt).replaceAll(envValue);
			}
		}
		txt
	}


	def readFromConfig()
	{

		if ( NamespaceInfo.count() == 0 )
		{
			wfsConfig.featureTypeNamespaces.each {
				NamespaceInfo.findOrSaveByPrefixAndUri( it.prefix, it.uri )
			}

			wfsConfig.datastores.each { datastore ->
				def workspaceInfo = WorkspaceInfo.findOrCreateByName( datastore.datastoreId )

				workspaceInfo.with {
					namespaceInfo = NamespaceInfo.findByPrefix( datastore.namespaceId )
					workspaceParams = [:]//datastore.datastoreParams
					datastore.datastoreParams.each{ k,v ->
						if (k.equals("Exposeprimarykeys")) {
							k = "Expose primary keys"
						}
						workspaceParams."${k}" = expandEnvVars(v);
					}
					save()
				}

				if ( workspaceInfo.hasErrors() )
				{
					workspaceInfo.errors.allErrors.each { log.info messageSource.getMessage( it, null ) }
				}
			}

			wfsConfig.featureTypes.each { featureType ->
				WorkspaceInfo.withTransaction {
					def workspaceInfo = WorkspaceInfo.findByName( featureType.datastoreId )

					if ( workspaceInfo ) {
						def layerInfo = LayerInfo.findOrCreateByNameAndWorkspaceInfo( featureType.name, workspaceInfo )

						layerInfo.with {
							title = featureType.title
							description = featureType.description
							keywords = featureType.keywords
							query = featureType.query
							geomName = featureType.geomName
							geomType = featureType.geomType
							geomSrs = featureType.geomSrs
						}

						workspaceInfo.addToLayerInfoList( layerInfo )
						workspaceInfo.save()

						if ( workspaceInfo.hasErrors() )
						{
							workspaceInfo.errors.allErrors.each { log.info messageSource.getMessage( it, null ) }
						}
					}
				}
			}
		}
	}
}
