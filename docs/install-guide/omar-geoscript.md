# OMAR Geoscript

## Purpose
The OMAR Geoscript application encapsulates all the geotools dependencies which provide libraries do things like draw footprints and query the OMAR database.

## Installation in Openshift

**Assumption:** The omar-geoscript-app docker image is pushed into the OpenShift server's internal docker registry and available to the project.

### Persistent Volumes

OMAR Geoscript does not require any persistent volumes.

### Environment variables

|Variable|Value|
|------|------|
|SPRING_PROFILES_ACTIVE|Comma separated profile tags (*e.g. production, dev*)|
|SPRING_CLOUD_CONFIG_LABEL|The Git branch from which to pull config files (*e.g. master*)|
