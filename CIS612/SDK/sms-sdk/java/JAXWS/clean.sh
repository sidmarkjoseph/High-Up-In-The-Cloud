#!/bin/sh

export SAMPLEJARDIR=./lib

rm -f ${SAMPLEJARDIR}/sms-samples.jar
rm -f ${SAMPLEJARDIR}/sms-apputils.jar

if [ "x${1}" != "x-w" ] 
then
   rm -f ${SAMPLEJARDIR}/sms.jar
   rm -rf samples/com/vmware/vim/
   rm -rf samples/com/vmware/vim25/
fi

echo Cleaning class files...

find samples/com/vmware -type f -name "*.class" -exec rm {} \;

echo Done cleaning class files.
