#!/bin/sh

## You need to set env variables : JAVA_HOME,
## or modify the value here

export SPBM_BUILD_DIR=$PWD
export SAMPLEDIR=${SPBM_BUILD_DIR}/samples
export JAVADOC=${SPBM_BUILD_DIR}/../../docs/java/JAXWS/samples/javadoc
export LIB_DIR=${SPBM_BUILD_DIR}/lib
export WSDLDIR=${SPBM_BUILD_DIR}/../../wsdl
export WSDLNAME=pbmService.wsdl
export PRODUCT=pbm
export PRODUCT_DISPLAY=VMware Storage Policy SDK

cd samples

# Check Java Home Env Variable
if [ -z "${JAVA_HOME}" ]; then
   echo "JAVA_HOME not defined. Must be defined to build java apps."
   exit
fi

export PATH="${JAVA_HOME}/bin:${PATH}"

if [ ! -e ${LIB_DIR} ]; then
   mkdir ${LIB_DIR}
fi

if [ ! -e ${JAVADOC} ]; then
   mkdir ${JAVADOC}
fi

# Copying Pre-requisites
echo Adding ssoclient.jar
if [ ! -f ${LIB_DIR}/ssoclient.jar ]; then
    if [ ! -f ${SPBM_BUILD_DIR}/../../../ssoclient/java/JAXWS/lib/ssoclient.jar ]; then
       echo building ssoclient.jar
       ${SPBM_BUILD_DIR}/../../../ssoclient/java/JAXWS/build.sh
    fi
    # On successful build
    echo copying ssoclient.jar
    cp ${SPBM_BUILD_DIR}/../../../ssoclient/java/JAXWS/lib/ssoclient.jar ${LIB_DIR}
fi

echo Adding ssosamples.jar
if [ ! -f ${LIB_DIR}/ssosamples.jar ]; then
    echo copying ssosamples.jar
    cp ${SPBM_BUILD_DIR}/../../../ssoclient/java/JAXWS/lib/ssosamples.jar ${LIB_DIR}
fi

echo Adding vSphere vim25.jar
if [ ! -f ${LIB_DIR}/vim25.jar ]; then
    if [ ! -f ${SPBM_BUILD_DIR}/../../../vsphere-ws/java/JAXWS/lib/vim25.jar ]; then
       echo building vim25.jar
       ${SPBM_BUILD_DIR}/../../../vsphere-ws/java/JAXWS/build.sh
    fi
    # On successful build
    echo copying vim25.jar
    cp ${SPBM_BUILD_DIR}/../../../vsphere-ws/java/JAXWS/lib/vim25.jar ${LIB_DIR}
fi

# Generate PBM Stubs

echo Generating $PRODUCT stubs from wsdl
xjc -episode common.episode $WSDLDIR/core-types.xsd -d . -p com.vmware.vim25
wsimport -wsdllocation $WSDLNAME -b common.episode -p com.vmware.$PRODUCT -s . ${WSDLDIR}/${WSDLNAME} -Xnocompile

echo Applying the FixJaxWsWsdlResource to PbmService.java
java -classpath ${LIB_DIR} FixJaxWsWsdlResource "${SPBM_BUILD_DIR}/samples/com/vmware/${PRODUCT}/PbmService.java" PbmService

echo Compiling $PRODUCT Stubs
javac -classpath $LIB_DIR/vim25.jar com/vmware/$PRODUCT/*.java

echo Copying the wsdl files
cp ${WSDLDIR}/* com/vmware/$PRODUCT/

echo Jar $PRODUCT Stubs
jar cf ${LIB_DIR}/${PRODUCT}.jar com/vmware/$PRODUCT/*.class com/vmware/$PRODUCT/*.wsdl com/vmware/$PRODUCT/*.xsd
jar cf ${LIB_DIR}/${PRODUCT}-src.jar com/vmware/$PRODUCT/*.java com/vmware/$PRODUCT/*.wsdl com/vmware/$PRODUCT/*.xsd

echo Cleaning generated code
rm -rf com/vmware/$PRODUCT
rm -rf com/vmware/vim25
rm -f common.episode

echo Compiling Samples
SAMPLE_SRC_FILES=`find "./com/vmware" -depth -name '*.java' -print`
javac -classpath "${LIB_DIR}/*" ${SAMPLE_SRC_FILES}

echo Jar Samples
SAMPLE_CLASS_FILES=`find "./com/vmware" -depth -name '*.class' -print`
jar cf ${LIB_DIR}/${PRODUCT}-samples.jar ${SAMPLE_CLASS_FILES}

echo Cleaning sample class files
rm ${SAMPLE_CLASS_FILES}

echo Generating Javadocs for samples
javadoc -classpath "${LIB_DIR}/*" -J-Xms512m -J-Xmx512m -d $JAVADOC -public -windowtitle "${PRODUCT_DISPLAY} Samples Documentation" -doctitle "<html><body>${PRODUCT_DISPLAY} Samples Reference Documentation<a name=topofpage></a" -nohelp ${SAMPLE_SRC_FILES}

echo Done.
