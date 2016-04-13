#!/bin/sh

SAMPLEJARDIR=./lib
PRODUCT=pbm

echo Cleaning Generated Jars
rm -f ${SAMPLEJARDIR}/${PRODUCT}-samples.jar ${SAMPLEJARDIR}/sso*.jar ${SAMPLEJARDIR}/vim25.jar ${SAMPLEJARDIR}/${PRODUCT}-src.jar ${SAMPLEJARDIR}/${PRODUCT}.jar
echo Cleaning Generated Samples Doc
rm -rf javadoc

echo ${RM_OUT}

echo Done
