@echo off
@REM you need to set env variables : JAVAHOME, VIMSDKHOME or modify the values below

set SAMPLEDIR=.

if NOT DEFINED JAVAHOME (
   echo JAVAHOME not defined. Must be defined to run java apps.
   goto END
)

if NOT DEFINED VIMSDKHOME (
   echo VIMSDKHOME not defined. Must be defined to run java apps.
   goto END
)

if NOT DEFINED VMKEYSTORE (
   echo VMKEYSTORE not defined. Must be defined to run java apps.
   goto END
)

setlocal

:SETENV
set PATH=%JAVAHOME%\bin;%PATH%
set LOCALCLASSPATH=%CD%\lib;%WBEMHOME%;%VIMSDKHOME%\java\JAXWS\lib\vim25.jar;
for %%i in ("lib\*.jar") do call lcp.bat %CD%\%%i
set LOCALCLASSPATH=%LOCALCLASSPATH%%CLASSPATH%

:next
if [%1]==[] goto argend   
   set ARG=%ARG% %1   
   shift
   goto next
:argend

:DORUN
pushd ..\..
"%JAVAHOME%"\bin\java -cp "%LOCALCLASSPATH%" -Djavax.net.ssl.trustStore="%VMKEYSTORE%" -Xmx1024M %ARG%
popd

endlocal

:END
echo Done.

