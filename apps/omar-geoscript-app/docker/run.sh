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

if [ "${JAVA_ARGS}" == "" ] ; then
   export JAVA_ARGS="-server -Xms256m -Xmx1024m -XX:+CMSClassUnloadingEnabled -XX:+UseGCOverheadLimit -XX:+UnlockExperimentalVMOptions -XX:MaxRAMFraction=1 -Djava.security.egd=file:/dev/./urandom"
fi

if [ "${KEY_STORE}" != "" ] ; then
   if [ "${JAVA_ARGS}" == "" ] ; then
      export JAVA_ARGS="-Djavax.net.ssl.keyStore=${KEY_STORE}"
   else
      export JAVA_ARGS="-Djavax.net.ssl.keyStore=${KEY_STORE} ${JAVA_ARGS}"
   fi
fi 

if [ "${KEY_STORE_PASSWORD}" != "" ] ; then
   if [ "${JAVA_ARGS}" == "" ] ; then
      export JAVA_ARGS="-Djavax.net.ssl.keyStorePassword=${KEY_STORE_PASSWORD}"
   else
      export JAVA_ARGS="-Djavax.net.ssl.keyStorePassword=${KEY_STORE_PASSWORD} ${JAVA_ARGS}"
   fi
fi 

if [ "${TRUST_STORE}" != "" ] ; then
   if [ "${JAVA_ARGS}" == "" ] ; then
      export JAVA_ARGS="-Djavax.net.ssl.trustStore=${TRUST_STORE}"
   else
      export JAVA_ARGS="-Djavax.net.ssl.trustStore=${TRUST_STORE} ${JAVA_ARGS}"
   fi
fi 

if [ "${TRUST_STORE_PASSWORD}" != "" ] ; then
   if [ "${JAVA_ARGS}" == "" ] ; then
      export JAVA_ARGS="-Djavax.net.ssl.trustStorePassword=${TRUST_STORE_PASSWORD}"
   else
      export JAVA_ARGS="-Djavax.net.ssl.trustStorePassword${TRUST_STORE_PASSWORD} ${JAVA_ARGS}"
   fi
fi

if [ "${REMOTE_DEBUG}" != "" ] ; then
   if [ "${JAVA_ARGS}" == "" ] ; then
      export JAVA_ARGS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
   else
      export JAVA_ARGS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 ${JAVA_ARGS}"
   fi
fi

export JAR_FILE=`find ${HOME} -name "*.jar"`
echo "Running command: java ${JAVA_ARGS} -jar ${JAR_FILE}"
java ${JAVA_ARGS} -jar ${JAR_FILE}
