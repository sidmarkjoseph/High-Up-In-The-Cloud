@echo off

set SAMPLEJARDIR=%CD%\lib

cd samples

if exist "%SAMPLEJARDIR%\samples.jar" del /F "%SAMPLEJARDIR%\samples.jar"

if NOT "x%1" == "x-w" (
   if exist "%SAMPLEJARDIR%\sms.jar" del /F "%SAMPLEJARDIR%\sms.jar"
   if exist com\vmware\vim\sms rmdir /q/s com\vmware\vim\sms >nul 2>nul
   if exist com\vmware\vim\sms rmdir /q/s com\vmware\vim\sms >nul 2>nul
)

del /s /q com\vmware\samples\sms\*.class >nul 2>nul

cd ..

:CLEANEND
@echo on