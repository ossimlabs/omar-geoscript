# omar-geoscript

## Description
The OMAR Geoscript application encapsulates all the geotools dependencies which provides libraries to query the OMAR database, draw footprints and manipulate shape files among other spatial capabilities.

[![Build Status](https://jenkins.ossim.io/buildStatus/icon?job=omar-geoscript-dev)]()

### Required environment variable
- OMAR_COMMON_PROPERTIES

### Optional environment variables
Only required for Jenkins pipelines or if you are running Nexus and/or Openshift locally

- OPENSHIFT_USERNAME
- OPENSHIFT_PASSWORD
- REPOSITORY_MANAGER_USER
- REPOSITORY_MANAGER_PASSWORD

## How to build/install omar-geoscript-app locally

1. Git clone the following repos or git pull the latest versions if you already have them.
```
  git clone https://github.com/ossimlabs/omar-common.git
  git clone https://github.com/ossimlabs/omar-core.git
  git clone https://github.com/ossimlabs/omar-openlayers.git
  git clone https://github.com/ossimlabs/omar-geoscript.git
```

2. Set OMAR_COMMON_PROPERTIES environment variable to the omar-common-properties.gradle (it is part of the omar-common repo).

3. Install omar-core-plugin (it is part of the omar-core repo).
```
 cd omar-core/plugins/omar-core-plugin
 gradle clean install
```

4. Install omar-openlayers-plugin
```
 cd omar-openlayers/plugins/omar-openlayers-plugin
 gradle clean install
```

5. Build/Install omar-geoscript-app
#### Build:
```
 cd omar-geoscript/apps/omar-geoscript-app
 gradle clean build
 ```
#### Install:
```
 cd omar-geoscript/apps/omar-geoscript-app
 gradle clean install
```
