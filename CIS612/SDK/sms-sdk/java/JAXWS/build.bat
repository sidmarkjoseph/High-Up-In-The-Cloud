@REM This batchfile generates JAXWS client stubs (proxy code) from sms and vim25
@REM versions of the WSDL and then compiles the client sample applications.
@REM
@REM To compile the client applications without re-generating the client stubs,
@REM pass the -w as an argument to the script. For example:
@REM build -w
@REM
@REM Note that this batchfile requires you to set two environment 
@REM variables:
@REM   JAVAHOME, VIMSDKHOME
@REM
@REM See the Developer's Setup Guide for more information about
@REM JAVAHOME.
@REM Alternatively, you can modify the settings of
@REM these three variables in the batchfile. Be careful if you do so.
@REM

@echo off

setlocal

if NOT DEFINED JAVAHOME (
   @echo JAVAHOME not defined. Must be defined to build java apps.
   goto END
)

if NOT DEFINED VIMSDKHOME (
   @echo VIMSDKHOME not defined. Must be defined to build java apps.
   goto END
)

set SAMPLEDIR="%CD%"\samples
set SAMPLEJARDIR="%CD%"

if NOT "x%1" == "x-w" (
   set SMSWSDLLOCATION=..\..\wsdl
   set WSDL_V1=version1\smService.wsdl
   set WSDL_V2=smsService.wsdl
)

:SETENV
set PATH=%JAVAHOME%\bin;%PATH%

set LOCALCLASSPATH="%CD%";"%CD%\lib";%VIMSDKHOME%\java\JAXWS\lib\vim25.jar;
for %%i in ("lib\*.jar") do call lcp.bat %CD%\%%i

:DOBUILD
call clean.bat %1

if NOT "x%1" == "x-w" (
   IF EXIST com (
      rmdir/s/q com
   )

   xcopy /q/i/s %SMSWSDLLOCATION%\*.* .
   xcopy /q/i/s %SMSWSDLLOCATION%\*.* com\vmware\vim\sms

   echo Generating sms stubs from wsdl

   %JAVAHOME%\bin\wsimport -wsdllocation %WSDL_V2% -b jaxb-customizations.xjb -b ws-customizations.xml -b vim-types.xsd -s . %WSDL_V2%

   @rem fix SmsService class to get the wsdl from the sms.jar
   %JAVAHOME%\bin\java -classpath %LOCALCLASSPATH% FixJaxWsWsdlResource "%CD%\com\vmware\vim\sms\SmsService.java"
   del /q/f com\vmware\vim\sms\SmsService.class
   %JAVAHOME%\bin\javac -classpath "%LOCALCLASSPATH%" com\vmware\vim\sms\SmsService.java

   %JAVAHOME%\bin\jar cf "%SAMPLEJARDIR%\sms.jar" com\vmware\vim25\*.class com\vmware\vim\sms\*.class com\vmware\vim\sms\*.wsdl class com\vmware\vim\sms\*.xsd

   del /q/f .\*.wsdl
   del /q/f .\*.xsd
   del /q/f com\vmware\vim\sms\*.wsdl
   del /q/f com\vmware\vim\sms\*.xsd
   del /q/f com\vmware\vim\sms\*.class
   del /q/f com\vmware\vim25\*.class

   echo Done generating sms stubs from wsdl
)

@rem allow for only compiling stub code, without regenerating java stub files
if "x%2" == "x-c" (
   echo Compiling sms stubs

   %JAVAHOME%\bin\javac -classpath "%LOCALCLASSPATH%" com\vmware\vim25\*.java com\vmware\vim\sms\*.java
   %JAVAHOME%\bin\jar cf "%SAMPLEJARDIR%\sms.jar" com\vmware\vim25\*.class com\vmware\vim\sms\*.class

   del /q/f com\vmware\vim\sms\*.class
   del /q/f com\vmware\vim25\*.class

   echo Done compiling sms stubs
)

cd %SAMPLEDIR%

@echo Compiling apputils
%JAVAHOME%\bin\javac -XDignore.symbol.file -classpath "%LOCALCLASSPATH%;%SAMPLEJARDIR%\sms.jar" com\vmware\apputils\*.java
%JAVAHOME%\bin\jar cf "%SAMPLEJARDIR%\sms-apputils.jar" com\vmware\apputils\*.class

@echo Compiling samples
%JAVAHOME%\bin\javac -XDignore.symbol.file -classpath "%LOCALCLASSPATH%;%SAMPLEJARDIR%\sms.jar;%SAMPLEJARDIR%\sms-apputils.jar" com\vmware\samples\sms\*.java
%JAVAHOME%\bin\jar cf "%SAMPLEJARDIR%\sms-samples.jar" com\vmware\samples\sms\*.class

cd ..

:END
@echo Done.
@echo on
