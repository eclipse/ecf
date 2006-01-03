echo off
setlocal
cd %~dp0
set RP=..\..\..\plugins
set ECF=%RP%\org.eclipse.ecf_0.6.0\ecf.jar
set UI=%RP%\org.eclipse.ecf.ui_0.6.0\ui.jar
set SDO=%RP%\org.eclipse.ecf.sdo_0.6.0\ecf.sdo.jar
set DS=%RP%\org.eclipse.ecf.datashare_0.6.0\datashare.jar
set PROVIDER=%RP%\org.eclipse.ecf.provider_0.6.0\provider.jar
set PRESENCE=%RP%\org.eclipse.ecf.presence_0.6.0\presence.jar
set GED=%RP%\org.eclipse.ecf.example.sdo.gefeditor_0.6.0\editor.jar
set ED=%RP%\org.eclipse.ecf.example.sdo.editor_0.6.0\editor.jar
set LIBRARY=%RP%\org.eclipse.ecf.example.sdo.library_0.6.0\runtime\org.eclipse.ecf.example.library.jar
set DISCOVERY=%RP%\org.eclipse.ecf.discovery_0.6.0\discovery.jar
set HELLO=%RP%\org.eclipse.ecf.example.hello_0.6.0\hello.jar
set COLLAB=%RP%\org.eclipse.ecf.example.collab_0.6.0\client.jar

set CP="..\lib\core.jar;..\lib\runtime.jar;..\lib\osgi.jar;%ECF%;%UI%;%SDO%;%PROVIDER%;%PRESENCE%;%GED%;%ED%;%LIBRARY%;%HELLO%;%DS%;%DISCOVERY%;%COLLAB%;."

set TRACE=-Dorg.eclipse.ecf.Trace=true -Dorg.eclipse.ecf.provider.Trace=true

set OPTIONS=

set MAINCLASS=org.eclipse.ecf.provider.app.ServerApplication
set ARGS=-c ..\conf\server.xml %*

rem Start server
echo "Starting server with options: %OPTIONS% and args: %ARGS%"
java -cp %CP% %OPTIONS% %MAINCLASS% %ARGS% 

endlocal