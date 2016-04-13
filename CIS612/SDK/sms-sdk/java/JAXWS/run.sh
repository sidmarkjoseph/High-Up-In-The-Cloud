#!/bin/sh

## you need to set env variables : JAVAHOME, VIMSDKHOME VMKEYSTORE or modify the 3 values here

export SAMPLEDIR=.

if [ "x${JAVAHOME}" = "x" ]
then
   echo JAVAHOME not defined. Must be defined to run java apps.
   exit
fi

if [ "x${VIMSDKHOME}" = "x" ]
then
   echo VIMSDKHOME not defined. Must be defined to run java apps.
   exit
fi

if [ "x${VMKEYSTORE}" = "x" ]
then
   echo VMKEYSTORE not defined. Must be defined to run java apps.
   exit
fi

export PATH=${JAVAHOME}/bin:${PATH}

LOCALCLASSPATH=${PWD}/lib:${PWD}/lib/sms.jar:${PWD}/lib/sms-samples.jar:${PWD}/lib/sms-apputils.jar:${VIMSDKHOME}/java/JAXWS/lib/vim25.jar

userroot=~
exec ${JAVAHOME}/bin/java  -classpath ${LOCALCLASSPATH} "-Djavax.net.ssl.trustStore=${VMKEYSTORE}" -Xmx1024M "$@"
