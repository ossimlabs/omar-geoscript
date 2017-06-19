#!/bin/bash login
pushd `dirname $0` >/dev/null
SCRIPT_DIR=`pwd -P`
pushd $SCRIPT_DIR/.. >/dev/null
WORKDIR=$PWD
popd > /dev/null
popd >/dev/null
gradle :omar-geoscript-app:artifactoryPublish
gradle :omar-geoscript-plugin:artifactoryPublish
