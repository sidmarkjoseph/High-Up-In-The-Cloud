#!/bin/sh
# set to relative path from this script to java libraries to use for classpath
export LIBDIR=./lib
# export LIBDIR=../lib

# encode non-optional system properties here
# sample.properties.files is a comma delimited list of file names to use
# for additional system properties.
export SAMPLE_PROPERTIES=" -Dsample.properties.files=$(echo *.properties | tr ' ' ',') "

# set max heap size here
export XMX_SIZE=1024M

export MAIN_CLASS=com.vmware.common.Main

# calculates JAVA_HOME
if [ "x${JAVA_HOME}" = "x" ]
then
   # some of our samples use JAVAHOME instead of JAVA_HOME allow for both.
   if [ "x${JAVAHOME}" = "x" ]
   then
      echo JAVA_HOME not defined. Must be defined to run java apps.
      exit
   fi
   export JAVA_HOME="${JAVAHOME}"
fi

exec ${JAVA_HOME}/bin/java ${SAMPLE_PROPERTIES} \
     -cp $(echo $LIBDIR/*.jar | tr ' ' ':') \
     -Xmx$XMX_SIZE \
     $MAIN_CLASS "$@"
