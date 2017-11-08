#!/bin/bash

#if [ -z ${KEY_STORE} ] ; then
#  export KEY_STORE=${HOME}/es/admin.jks
#fi

#if [ -z ${KEY_STORE_PASSWORD} ] ; then
#  export KEY_STORE_PASSWORD=kspass
#fi

#if [ -z ${TRUST_STORE} ] ; then
#   export TRUST_STORE=${HOME}/es/truststore
#fi

if [ -z $JAVA_PROPERTIES ] ; then
   export JAVA_PROPERTIES="-server -Xms256m -Xmx1024m -XX:+CMSClassUnloadingEnabled -XX:+UseGCOverheadLimit -XX:+UnlockExperimentalVMOptions  -XX:+UseCGroupMemoryLimitForHeap -XX:MaxRAMFraction=1 -Djava.security.egd=file:/dev/./urandom"
fi

if [ "${KEY_STORE}" != "" ] ; then
   if [ "${JAVA_PROPERTIES}" == "" ] ; then
      export JAVA_PROPERTIES="-Djavax.net.ssl.keyStore=${KEY_STORE}"
   else
      export JAVA_PROPERTIES="-Djavax.net.ssl.keyStore=${KEY_STORE} ${JAVA_PROPERTIES}"
   fi
fi 

if [ "${KEY_STORE_PASSWORD}" != "" ] ; then
   if [ "${JAVA_PROPERTIES}" == "" ] ; then
      export JAVA_PROPERTIES="-Djavax.net.ssl.keyStorePassword=${KEY_STORE_PASSWORD}"
   else
      export JAVA_PROPERTIES="-Djavax.net.ssl.keyStorePassword=${KEY_STORE_PASSWORD} ${JAVA_PROPERTIES}"
   fi
fi 

if [ "${TRUST_STORE}" != "" ] ; then
   if [ "${JAVA_PROPERTIES}" == "" ] ; then
      export JAVA_PROPERTIES="-Djavax.net.ssl.trustStore=${TRUST_STORE}"
   else
      export JAVA_PROPERTIES="-Djavax.net.ssl.trustStore=${TRUST_STORE} ${JAVA_PROPERTIES}"
   fi
fi 

if [ "${TRUST_STORE_PASSWORD}" != "" ] ; then
   if [ "${JAVA_PROPERTIES}" == "" ] ; then
      export JAVA_PROPERTIES="-Djavax.net.ssl.trustStorePassword=${TRUST_STORE_PASSWORD}"
   else
      export JAVA_PROPERTIES="-Djavax.net.ssl.trustStorePassword${TRUST_STORE_PASSWORD} ${JAVA_PROPERTIES}"
   fi
fi 

java $JAVA_PROPERTIES -jar *.jar
