# OMAR Geoscript

## Dockerfile
```
FROM omar-ossim-base
EXPOSE 8080
RUN mkdir /usr/share/omar
COPY omar-geoscript-app-1.0.0-SNAPSHOT.jar /usr/share/omar
RUN chown -R 1001:0 /usr/share/omar
RUN chown 1001:0 /usr/share/omar
RUN chmod -R g+rw /usr/share/omar
RUN find $HOME -type d -exec chmod g+x {} +
USER 1001
WORKDIR /usr/share/omar
CMD java -server -Xms256m -Xmx1024m -Djava.awt.headless=true -XX:+CMSClassUnloadingEnabled -XX:+UseGCOverheadLimit -Djava.security.egd=file:/dev/./urandom -jar omar-geoscript-app-1.0.0-SNAPSHOT.jar
```
Ref: [omar-ossim-base](../../../omar-ossim-base/docs/install-guide/omar-ossim-base/)

## JAR
`http://artifacts.radiantbluecloud.com/artifactory/webapp/#/artifacts/browse/tree/General/omar-local/io/ossim/omar/apps/omar-geoscript-app`

## Configuration
You will need to insert the [omar-common](../../../omar-common/docs/install-guide/omar-common#common-config-settings)

```
wfs:
  featureTypeNamespaces:
      - prefix: omar
        uri: http://omar.ossim.org

  datastores:
      - namespaceId: omar
        datastoreId: omar_prod
        datastoreParams:
          dbtype: postgis
          host: ${omarDb.host}
          port: ${omarDb.port}
          database: ${omarDb.name}
          user: ${omarDb.username}
          passwd: ${omarDb.password}
          'Expose primary keys': 'true'
          namespace: http://omar.ossim.org

  featureTypes:
      - name: raster_entry
        title: raster_entry
        description: ''
        keywords:
          - omar
          - raster_entry
          - features
        datastoreId: omar_prod

      - name: video_data_set
        title: video_data_set
        description: ''
        keywords:
          - omar
          - video_data_set
          - features
        datastoreId: omar_prod

```
