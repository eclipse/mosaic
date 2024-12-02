#!/bin/bash
set -e

# set maximum JVM memory
javaMemorySizeXmx="2g"

# uncomment to activate remote debugging
# javaRemoteDebugging="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=10000"

# check if JAVA_HOME is set and define path to java binary accordingly (variable: java_bin)
if [ -z "${JAVA_HOME}" ]; then
  java_bin=java
else
  java_bin=${JAVA_HOME}/bin/java
fi

# mosaic
dir_mosaic=./lib/mosaic
tmp=`ls ${dir_mosaic} | grep jar`
mosaic=${dir_mosaic}/${tmp//[^A-Za-z0-9\-\.]/:${dir_mosaic}/}

# third-party
dir_libs=./lib/third-party
tmp=`ls ${dir_libs} | grep jar`
libs=${dir_libs}/${tmp//[^A-Za-z0-9\-\.]/:${dir_libs}/}

# create and run command
"${java_bin}" -Xmx${javaMemorySizeXmx} ${javaRemoteDebugging} -cp .:${mosaic}:${libs} org.eclipse.mosaic.starter.MosaicStarter "$@"