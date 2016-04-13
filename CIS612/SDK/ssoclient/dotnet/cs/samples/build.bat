@setlocal
@echo off
rem Generate SSO WebService Stubs

rem Please set the environment variable WSE_HOME before running this script to the
rem install directory for Web Services Enhancements 3.0 for Microsoft .NET aka WSE
if not defined WSE_HOME goto err_no_WSEENVVARIABLE
if not exist "%WSE_HOME%\Microsoft.Web.Services3.dll" goto err_no_WSEENVSETUP
if not exist "%WSE_HOME%\Tools\WseWsdl3.exe" goto err_no_WSEENVSETUP

@if "%VSINSTALLDIR%"=="" goto error_no_VSINSTALLDIR

rem Verifying the availability of the commands before proceeding

pvk2pfx /?>nul 2>nul
if %ERRORLEVEL% GEQ 1 goto err_pvk2pfx

makecert /?>nul 2>nul
if %ERRORLEVEL% GEQ 1 goto err_makecert

csc /?>nul 2>nul
if %ERRORLEVEL% GEQ 1 goto err_csc

msbuild /?>nul 2>nul
if %ERRORLEVEL% GEQ 1 goto err_msbuild

if "%1"=="/?" goto print_help
if "%1"=="--help" goto print_help
set MODE=-1
if [%1]==[] set MODE=0
if "%1"=="-s" set MODE=0
if "%1"=="-w" set MODE=1
if "%1"=="-b" set MODE=2
if %MODE%==-1 goto err_wrong_parameters

if [%2]==[] (
   set _WSDLDIR=..\..\..\wsdl
) else (
   set _WSDLDIR=%2
)
if not exist %_WSDLDIR%\STSService.wsdl goto err_no_WSDLFILE

if [%3]==[] (
   set _STUBDIR=lib
) else (
   set _STUBDIR=%3
)

if [%4]==[] (
   set _DLLDIR=lib
) else (
   set _DLLDIR=%4

)

echo Setting up certificate and lib directories
if not exist certificate (
	mkdir certificate
)
del /q/f certificate\*>nul 2>nul

if not exist lib (
	mkdir lib
)
del /q/f lib\*>nul 2>nul

set _WSDLFILES=
for %%i in ("%_WSDLDIR%\*") do call lcp.bat %CD%\%%i

if %MODE%==0 goto build_stubs
if %MODE%==2 goto build_stubs
if %MODE%==1 goto build_samples

:build_stubs 
echo Generating Stubs...
"%WSE_HOME%"\Tools\WseWsdl3.exe /out:%_STUBDIR% /type:webClient %_WSDLFILES%
echo Compiling Stubs...
csc /target:library /out:%_DLLDIR%\STSService.dll %_STUBDIR%\STSService.cs /reference:"%WSE_HOME%\Microsoft.Web.Services3.dll"
del %_STUBDIR%\STSService.cs

:certificate
echo Generating Test Self Signed Certificate and Private Key...
makecert -r -pe -n "CN=*.vmware.com, OU=Ecosystem Engineering, O=\"VMware, Inc.\", L=Palo Alto, ST=California, C=US" -sky exchange -sv certificate\testssoclient.pvk certificate\testssoclient.cer
pvk2pfx -pvk certificate\testssoclient.pvk -spc certificate\testssoclient.cer -pfx certificate\testssoclient.pfx
del certificate\testssoclient.pvk
del certificate\testssoclient.cer

if %MODE%==0 goto end_ok

:build_samples
echo Building Samples...
MSBuild /nologo SSOSample.sln /t:Rebuild /p:Configuration=Release
if %ERRORLEVEL%==0 (
   echo C# SSO Samples compiled successfully...
) else (
   echo Error: C# SSO Samples compilation failed! See the errors above!
   goto end_err
)
goto end_ok

:err_no_WSDLFILE
echo Error: Directory for WSDL files STSService.wsdl not specified
echo        Please specify WSDL files to generate stubs for
goto end_err

:err_no_DOTNETINSTALL
echo Error: No .Net Framework Installation found at %DOTNET_INSTALL% location
goto end_err

:err_no_WSEENVSETUP
echo Error: No WSE 3.0 Installation found at %WSE_HOME%
goto end_err

:err_no_WSEENVVARIABLE
echo Error: WSE_HOME Environment Variable Not Found. Please install WSE 3.0 and set WSE_HOME variable to the install directory e.g. C:\Program Files (x86)\Microsoft WSE\v3.0 
goto end_err

:error_no_VSINSTALLDIR
echo Error: No Visual Studio environment settings found
echo        Please run the script inside a Visual Studio Command Prompt
goto end_err

:end_err
echo Build Failed!
goto end

:err_pvk2pfx
echo Error: pvk2pfx command not found. 
echo        Please ensure that you have added path to pvk2pfx.exe to the PATH variable
goto end

:err_makecert
echo Error: makecert command not found. 
echo        Please ensure that you have added path to makecert.exe to the PATH variable
goto end

:err_csc
echo Error: csc command not found. 
echo        Please ensure that you have added path to csc.exe to the PATH variable
goto end

:err_msbuild
echo Error: MSBuild command not found. 
echo        Please ensure that you have added path to MSbuild.exe to the PATH variable
goto end

:err_wrong_parameters
echo Please check the parameters supplied..
goto print_help

:print_help
echo ---------------------------HELP-------------------------------
echo Build Utility for generating the C# vCenter Single Sign-On Stubs
echo The utility also generates a test certificate for the samples
echo This script takes 4 optional parameters in the following mandatory order:
echo 1. flag for building Stubs only(-s)/Samples only(-w)/Both(-b)
echo     This defaults to (-s) if ommited.
echo 2. directory of wsdl files.
echo     This defaults to ..\..\..\wsdl diretory
echo 3. stub output directory
echo     This defaults to current directory
echo 4. dll directory
echo     This defaults to current directory
echo 
echo build.bat --help or build.bat /? prints this help message
echo --------------------------------------------------------------
goto end

:end_ok
echo Build Completed!

:end
endlocal

