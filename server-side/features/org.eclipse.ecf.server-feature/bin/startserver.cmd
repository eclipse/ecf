echo off
setlocal
cd %~dp0
set RP=..\..\..\plugins
set ECF=%RP%\org.eclipse.ecf_0.6.2\ecf.jar
set UI=%RP%\org.eclipse.ecf.ui_0.6.2\ui.jar
set SDO=%RP%\org.eclipse.ecf.sdo_0.6.2\ecf.sdo.jar
set DS=%RP%\org.eclipse.ecf.datashare_0.6.2\datashare.jar
set DSP=%RP%\org.eclipse.ecf.provider.datashare_0.6.2\dsprovider.jar
set FS=%RP%\org.eclipse.ecf.fileshare_0.6.2\fileshare.jar
set FSP=%RP%\org.eclipse.ecf.provider.fileshare_0.6.2\fsprovider.jar
set PROVIDER=%RP%\org.eclipse.ecf.provider_0.6.2\provider.jar
set PRESENCE=%RP%\org.eclipse.ecf.presence_0.6.2\presence.jar
set GED=%RP%\org.eclipse.ecf.example.sdo.gefeditor_0.6.2\editor.jar
set ED=%RP%\org.eclipse.ecf.example.sdo.editor_0.6.2\editor.jar
set LIBRARY=%RP%\org.eclipse.ecf.example.sdo.library_0.6.2\runtime\org.eclipse.ecf.example.library.jar
set DISCOVERY=%RP%\org.eclipse.ecf.discovery_0.6.2\discovery.jar
set HELLO=%RP%\org.eclipse.ecf.example.hello_0.6.2\hello.jar
set COLLAB=%RP%\org.eclipse.ecf.example.collab_0.6.2\client.jar

set CP="..\lib\core.jar;..\lib\runtime.jar;..\lib\osgi.jar;%ECF%;%UI%;%SDO%;%PROVIDER%;%PRESENCE%;%GED%;%ED%;%LIBRARY%;%HELLO%;%DS%;%DSP%;%FS%;%FsSP%;%DISCOVERY%;%COLLAB%;."

set TRACE=-Dorg.eclipse.ecf.Trace=true -Dorg.eclipse.ecf.provider.Trace=true

set OPTIONS=

set MAINCLASS=org.eclipse.ecf.provider.app.ServerApplication
set ARGS=-c ..\conf\server.xml %*

rem Start server
echo "Starting server with options: %OPTIONS% and args: %ARGS%"
java -cp %CP% %OPTIONS% %MAINCLASS% %ARGS% 

endlocal