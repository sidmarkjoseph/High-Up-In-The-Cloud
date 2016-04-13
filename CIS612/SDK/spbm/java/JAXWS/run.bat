@echo off
setlocal enableextensions enabledelayedexpansion

@REM relative path to library directory holding JAR files
set LIB_DIR=lib

:SETPROPERTIES
@REM this section builds a list of properties files to use
@REM sample.properties.files is a comma delimited list of non-optional file names
set PROPERTIES_FILES=
for %%i in (*.properties) do (
	set PROPERTIES_FILES=!PROPERTIES_FILES!,%%i
)
for /f "tokens=* delims=," %%a in ("%PROPERTIES_FILES%") do set PROPERTIES_FILES=%%~a

@REM set non-optional system properties here
set SAMPLE_PROPERTIES= -Dsample.properties.files=%PROPERTIES_FILES%

:CALCJAVAHOME
@REM calculate the JAVA_HOME
if NOT DEFINED JAVA_HOME (
   if NOT DEFINED JAVAHOME (
       @echo JAVA_HOME not defined. Must be defined to run java apps.
       goto END
   )
   else (
       set JAVA_HOME=%JAVAHOME%
   )
)
:SETENV
set PATH=%JAVA_HOME%\bin;%PATH%

:CALCLOCALCLASSPATH
set LOCALCLASSPATH=
for %%i in ("%LIB_DIR%\*.jar") do (
    set LOCALCLASSPATH=!LOCALCLASSPATH!;%%i
)
set LOCALCLASSPATH=%LOCALCLASSPATH%;%CLASSPATH%
for /f "tokens=* delims=;" %%a in ("%LOCALCLASSPATH%") do set LOCALCLASSPATH=%%~a
for /l %%a in (1,1,2) do if "!LOCALCLASSPATH:~-1!"==";" set LOCALCLASSPATH=!LOCALCLASSPATH:~0,-1!

setlocal
:next
if [%1]==[] goto argend
   set ARG=%ARG% %1
   shift
   goto next
:argend

:DORUN

"%JAVA_HOME%"\bin\java %SAMPLE_PROPERTIES% -cp "%LOCALCLASSPATH%" -Xmx1024M com.vmware.common.Main %ARG%

endlocal

:END
echo Done.
