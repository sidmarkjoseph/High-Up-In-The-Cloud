#!/bin/sh

## You need to set env variables : JAVAHOME, VIMSDKHOME
## or modify the value here

export SAMPLEDIR=${PWD}/samples
export SAMPLEJARDIR=${PWD}/lib

if [ "x${JAVAHOME}" = "x" ]
then
   echo JAVAHOME not defined. Must be defined to build java apps.
   exit
fi

if [ "x${VIMSDKHOME}" = "x" ]
then
   echo VIMSDKHOME not defined. Must be defined to build java apps.
   exit
fi

if [ "x${1}" != "x-w" ] 
then 
   export WSDL_V1=version1/smService.wsdl
   export WSDL_V2=smsService.wsdl
fi

export PATH="${JAVAHOME}/bin:${PATH}"

./clean.sh ${1}

LOCALCLASSPATH=${CLASSPATH}:${SAMPLEJARDIR}/lib:${SAMPLEJARDIR}/sms.jar:${SAMPLEJARDIR}/sms-samples.jar:${SAMPLEJARDIR}/sms-apputils.jar:${VIMSDKHOME}/java/JAXWS/lib/vim25.jar

if [ "x${1}" != "x-w" ] 
then
   echo Generating sms stubs from wsdl

   mkdir -p samples/com/vmware/vim/sms
   mkdir -p samples/com/vmware/vim25

   cp -f ../../wsdl/*.* .
   cp -f ../../wsdl/*.* samples/com/vmware/vim/sms

   ${JAVAHOME}/bin/wsimport -wsdllocation ${WSDL_V2} -b jaxb-customizations.xjb -b ws-customizations.xml -b vim-types.xsd -s ./samples ${WSDL_V2}

   cd samples

   ## fix SmsService class to get the wsdl from the sms.jar
   ${JAVAHOME}/bin/java -classpath ${CLASSPATH}:${SAMPLEJARDIR} FixJaxWsWsdlResource ./com/vmware/vim/sms/SmsService.java

   echo Done generating sms stubs. Compiling sms stubs.
   ${JAVAHOME}/bin/javac -J-Xms1024M -J-Xmx1024M -classpath ${LOCALCLASSPATH}:. com/vmware/vim25/*.java com/vmware/vim/sms/*.java
   ${JAVAHOME}/bin/jar cf ${SAMPLEJARDIR}/sms.jar com/vmware/vim25/*.class com/vmware/vim/sms/*.class com/vmware/vim/sms/*.wsdl com/vmware/vim/sms/*.xsd

   rm -f com/vmware/vim25/*.class com/vmware/vim/sms/*.class com/vmware/vim/sms/*.wsdl com/vmware/vim/sms/*.xsd ../*.wsdl ../*.xsd

   echo Done compiling sms stubs.
fi

## allow for only compiling stub code, without regenerating java stub files
if [ "x${2}" = "x-c" ]
then
   echo Compiling sms stubs

   cp -f ../../wsdl/*.* samples/com/vmware/vim/sms

   cd samples 

   ${JAVAHOME}/bin/javac -J-Xms1024M -J-Xmx1024M -classpath ${LOCALCLASSPATH}:. com/vmware/vim25/*.java com/vmware/vim/sms/*.java
   ${JAVAHOME}/bin/jar cf ${SAMPLEJARDIR}/sms.jar com/vmware/vim25/*.class com/vmware/vim/sms/*.class com/vmware/vim/sms/*.wsdl com/vmware/vim/sms/*.xsd

   rm -f com/vmware/vim25/*.class com/vmware/vim/sms/*.class com/vmware/vim/sms/*.wsdl com/vmware/vim/sms/*.xsd

   echo Done compiling sms stubs
fi

echo Compiling samples

find ./com/vmware/apputils -depth -name '*.java' -print > files_src.txt
${JAVAHOME}/bin/javac -J-Xms256M -J-Xmx256M -XDignore.symbol.file -classpath ${LOCALCLASSPATH}:. @files_src.txt
${JAVAHOME}/bin/jar cf ${SAMPLEJARDIR}/sms-apputils.jar com/vmware/apputils/*.class
rm files_src.txt

find ./com/vmware/samples -depth -name '*.java' -print > files_src.txt
${JAVAHOME}/bin/javac -J-Xms256M -J-Xmx256M -XDignore.symbol.file -classpath ${LOCALCLASSPATH}:. @files_src.txt
${JAVAHOME}/bin/jar cf ${SAMPLEJARDIR}/sms-samples.jar com/vmware/samples/sms/*.class
rm files_src.txt

cd ..

echo Done.
