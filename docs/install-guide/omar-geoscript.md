# OMAR Geoscript

## Source Location
https://github.com/ossimlabs/omar-geoscript

## Purpose
The OMAR Geoscript application encapsulates all the geotools dependencies which provide libraries do things like draw footprints and query the OMAR database.

## Dockerfile
```
FROM omar-base
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
Ref: [omar-base](../../../omar-base/docs/install-guide/omar-base/)

## JAR
[https://artifactory.ossim.ioartifactory/webapp/#/artifacts/browse/tree/General/omar-local/io/ossim/omar/apps/omar-geoscript-app](https://artifactory.ossim.io/artifactory/webapp/#/artifacts/browse/tree/General/omar-local/io/ossim/omar/apps/omar-geoscript-app)

## Configuration
You will need to insert the [Common Config Settings](../../../omar-common/docs/install-guide/omar-common/#common-config-settings).

## Installation in Openshift

**Assumption:** The omar-geoscript-app docker image is pushed into the OpenShift server's internal docker registry and available to the project.

### Persistent Volumes

OMAR Geoscript does not require any persistent volumes.

### Environment variables

|Variable|Value|
|------|------|
|SPRING_PROFILES_ACTIVE|Comma separated profile tags (*e.g. production, dev*)|
|SPRING_CLOUD_CONFIG_LABEL|The Git branch from which to pull config files (*e.g. master*)|

### An Example DeploymentConfig
```yaml
apiVersion: v1
kind: DeploymentConfig
metadata:
  annotations:
    openshift.io/generated-by: OpenShiftNewApp
  creationTimestamp: null
  generation: 1
  labels:
    app: omar-openshift
  name: omar-geoscript-app
spec:
  replicas: 1
  selector:
    app: omar-openshift
    deploymentconfig: omar-geoscript-app
  strategy:
    activeDeadlineSeconds: 21600
    resources: {}
    rollingParams:
      intervalSeconds: 1
      maxSurge: 25%
      maxUnavailable: 25%
      timeoutSeconds: 600
      updatePeriodSeconds: 1
    type: Rolling
  template:
    metadata:
      annotations:
        openshift.io/generated-by: OpenShiftNewApp
      creationTimestamp: null
      labels:
        app: omar-openshift
        deploymentconfig: omar-geoscript-app
    spec:
      containers:
      - env:
        - name: SPRING_PROFILES_ACTIVE
          value: dev
        - name: SPRING_CLOUD_CONFIG_LABEL
          value: master
        image: 172.30.181.173:5000/o2/omar-geoscript-app@sha256:b972d0890e3de500efac96a980d26e336b059bfde49e2332e7bafe651dfb7fed
        imagePullPolicy: Always
        livenessProbe:
          failureThreshold: 3
          initialDelaySeconds: 30
          periodSeconds: 10
          successThreshold: 1
          tcpSocket:
            port: 8080
          timeoutSeconds: 5
        name: omar-geoscript-app
        ports:
        - containerPort: 8080
          protocol: TCP
        readinessProbe:
          failureThreshold: 3
          initialDelaySeconds: 20
          periodSeconds: 10
          successThreshold: 1
          tcpSocket:
            port: 8080
          timeoutSeconds: 5
        resources: {}
        terminationMessagePath: /dev/termination-log
      dnsPolicy: ClusterFirst
      restartPolicy: Always
      securityContext: {}
      terminationGracePeriodSeconds: 30
  test: false
  triggers:
  - type: ConfigChange
  - imageChangeParams:
      automatic: true
      containerNames:
      - omar-geoscript-app
      from:
        kind: ImageStreamTag
        name: omar-geoscript-app:latest
        namespace: o2
    type: ImageChange
status:
  availableReplicas: 0
  latestVersion: 0
  observedGeneration: 0
  replicas: 0
  unavailableReplicas: 0
  updatedReplicas: 0
```
