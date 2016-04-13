@echo off
setlocal enableextensions enabledelayedexpansion
set SPBM_BUILD_DIR=%CD%
set LIB_DIR=%CD%\lib
set JAVADOC=%CD%\..\..\docs\java\JAXWS\samples\javadoc
set WSDLDIR=%CD%\..\..\wsdl
set WSDLNAME=pbmService.wsdl
set PRODUCT=pbm
set PRODUCT_DISPLAY=VMware Storage Policy SDK

cd samples

:SETJAVAENV
if NOT DEFINED JAVA_HOME (
   if NOT DEFINED JAVAHOME (
       echo JAVA_HOME not defined. Must be defined to build java apps.
       goto ERROR
   ) else (
       set JAVA_HOME=%JAVAHOME%
   )
)
set PATH="%JAVA_HOME%"\bin;%PATH%

IF NOT EXIST %LIB_DIR% (
  mkdir %LIB_DIR%
)

IF NOT EXIST %JAVADOC% (
  mkdir %JAVADOC%
)

:PARAMETERS
@rem Parsing the command line arguments
@rem set the defaults
set MODE=2

set READ=0
SetLocal ENABLEDELAYEDEXPANSION
FOR %%a IN (%*) DO (
  if !READ!==1 (
      echo setting WSDL dir to %%a
      set WSDLDIR=%%a
      set READ=0
  ) else (
    if /I "%%a"=="-help" goto PRINT_HELP
    if /I "%%a"=="-s" set MODE=0
    if /I "%%a"=="-w" set MODE=1
    if /I "%%a"=="-b" set MODE=2
    if /I "%%a"=="-wsdldir" set READ=1
  )
)

if %MODE%==0 goto BUILD_STUBS
if %MODE%==2 goto BUILD_STUBS
if %MODE%==1 goto BUILD_SAMPLES


:BUILD_STUBS
:COPYING_PREREQUSITES
echo Adding ssoclient.jar.....
xcopy/y/q/i/s %SPBM_BUILD_DIR%\..\..\..\ssoclient\java\JAXWS\lib\ssoclient.jar %LIB_DIR% || goto ERROR

echo Adding vim25.jar.....
IF NOT EXIST %SPBM_BUILD_DIR%\..\..\..\vsphere-ws\java\JAXWS\lib\vim25.jar (
	echo Missing vim25.jar... Trigerring the vsphere stub generator build to generate one.
	pushd %SPBM_BUILD_DIR%\..\..\..\vsphere-ws\java\JAXWS || goto ERROR
	call build.bat -s || goto ERROR
	popd
)
xcopy/y/q/i/s %SPBM_BUILD_DIR%\..\..\..\vsphere-ws\java\JAXWS\lib\vim25.jar %LIB_DIR% || goto ERROR

echo Adding ssosamples.jar.....
if not exist %SPBM_BUILD_DIR%\..\..\..\ssoclient\java\JAXWS\lib\ssosamples.jar (
	echo Missing ssosamples.jar... Trigerring the ssoclient build to generate one.
	pushd %SPBM_BUILD_DIR%\..\..\..\ssoclient\java\JAXWS || goto ERROR
	call build.bat || goto ERROR
	popd
)
xcopy/y/q/i/s %SPBM_BUILD_DIR%\..\..\..\ssoclient\java\JAXWS\lib\ssosamples.jar %LIB_DIR% || goto ERROR

:GENERATE_STUBS
echo Generating %PRODUCT% stubs from wsdl
xjc -episode common.episode %WSDLDIR%\core-types.xsd -d . -p com.vmware.vim25 >nul 2>nul || goto ERROR
wsimport -wsdllocation "%WSDLNAME%" -b common.episode -p com.vmware.%PRODUCT% -s . "%WSDLDIR%\%WSDLNAME%" -Xnocompile || goto ERROR
@rem fix PbmService class to get the wsdl from the jar
echo Applying the FixJaxWsWsdlResource to PbmService.java
java -classpath %LIB_DIR%\ FixJaxWsWsdlResource "%SPBM_BUILD_DIR%\samples\com\vmware\%PRODUCT%\PbmService.java" PbmService|| goto ERROR
del /q/f common.episode >nul 2>nul || goto ERROR

:COMPILING_STUBS
echo Compiling %PRODUCT% Stubs
javac -classpath %LIB_DIR%\vim25.jar com\vmware\%PRODUCT%\*.java || goto ERROR

:JARRING_STUBS
echo Copying the wsdl files
xcopy /q/i/s "%WSDLDIR%\*" com\vmware\%PRODUCT%\ || goto ERROR
echo Jarring %PRODUCT% Stubs
jar cf "%LIB_DIR%\%PRODUCT%.jar" com\vmware\%PRODUCT%\*.class com\vmware\%PRODUCT%\*.wsdl com\vmware\%PRODUCT%\*.xsd || goto ERROR
jar cf "%LIB_DIR%\%PRODUCT%-src.jar" com\vmware\%PRODUCT%\*.java com\vmware\%PRODUCT%\*.wsdl com\vmware\%PRODUCT%\*.xsd || goto ERROR

:CLEANINGSTUBS
echo Cleaning generated code
del /q/f com\vmware\%PRODUCT%\* >nul 2>nul || goto ERROR
rmdir /S /Q com\vmware\%PRODUCT% >nul 2>nul || goto ERROR
del /q/f com\vmware\vim25\* >nul 2>nul || goto ERROR
rmdir /S /Q com\vmware\vim25 >nul 2>nul || goto ERROR

if %MODE%==0 goto EOF

:BUILD_SAMPLES
:CALCLOCALCLASSPATH
set LOCALCLASSPATH=
for %%i in ("%LIB_DIR%\*.jar") do (
    set LOCALCLASSPATH=!LOCALCLASSPATH!;%%i
)
set LOCALCLASSPATH=%LOCALCLASSPATH%;%CLASSPATH%
for /f "tokens=* delims=;" %%a in ("%LOCALCLASSPATH%") do set LOCALCLASSPATH=%%~a
for /l %%a in (1,1,2) do if "!LOCALCLASSPATH:~-1!"==";" set LOCALCLASSPATH=!LOCALCLASSPATH:~0,-1!
setlocal

:JAVASCFILELIST
set JAVASRC=
for /L %%n in (1 1 500) do if "!__cd__:~%%n,1!" neq "" set /a "len=%%n+1"
for /r . %%g in (*.java) do (
  set absPath=%%g
  set relPath=!absPath:~%len%!
  set JAVASRC=!relPath! !JAVASRC!
)
echo %JAVASRC% > java_src.txt
echo Compiling samples
javac -classpath "%LOCALCLASSPATH%" @java_src.txt || goto ERROR

:CLASSFILELIST
set CLASSFILES=
for /L %%n in (1 1 500) do if "!__cd__:~%%n,1!" neq "" set /a "len=%%n+1"
for /r . %%g in (*.class) do (
  set absPath=%%g
  set relPath=!absPath:~%len%!
  set CLASSFILES=!relPath! !CLASSFILES!
)
echo %CLASSFILES% > class_files.txt
echo Jarring samples
jar cf ..\lib\%PRODUCT%-samples.jar @class_files.txt || goto ERROR
del /q/f/s *.class >nul 2>nul || goto ERROR
del /q/f class_files.txt >nul 2>nul || goto ERROR

:JAVADOCS
echo Generating javadocs for samples
javadoc -classpath "%LOCALCLASSPATH%" -J-Xms512m -J-Xmx512m -d %JAVADOC% -public -windowtitle "%PRODUCT_DISPLAY% Samples Documentation" -doctitle "<html><body>%PRODUCT_DISPLAY% Samples Reference Documentation<a name=topofpage></a>" -nohelp @java_src.txt >nul 2>nul || goto ERROR
del /q/f java_src.txt

goto EOF

:PRINT_HELP
echo ---------------------------HELP-------------------------------
echo Build utility for generating the %PRODUCT_DISPLAY% Stubs and compiling the samples
echo USAGE:
echo build.bat [-s,-w,-b] -wsdldir [directory]
echo .
echo -s build Stubs only
echo -w build Samples only
echo -b build both Stubs and Samples (Default in case none of -s or -w or -b is specified)
echo -wsdldir [directory] directory of wsdl files.
echo     This defaults to ..\..\wsdl directory
echo .
echo -help prints this help message
echo --------------------------------------------------------------
goto END


:ERROR
@echo Build Failed!
@endlocal
@exit /b 1

:EOF
echo Build Completed!
:END
@endlocal