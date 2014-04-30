@echo off
set JAVAPROG=java
set GENERICHOSTNAME=localhost
set GENERICPORT=3288
if "%1"=="" goto usage
set GENERICHOSTNAME=%1
:runjava
set JAVAPROPS=-Decf.generic.server.hostname=%GENERICHOSTNAME% -Decf.generic.server.port=%GENERICPORT% -Declipse.ignoreApp=true -Dosgi.noShutdown=true
echo javaprops=%JAVAPROPS%
set EQUINOXJAR=plugins/org.eclipse.osgi_3.10.0.v20140407-2102.jar
echo equinoxjar=%EQUINOXJAR%
set ARGS=-configuration file:configuration -os linux -ws gtk -arch arm -console -consoleLog -debug
echo program args=%ARGS%
%JAVAPROG% %JAVAPROPS% -jar %EQUINOXJAR% %ARGS%
goto end
:usage
ECHO No arguments supplied.  Usage:  %0 hostname [port]
:end
@echo on
