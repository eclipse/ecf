# ECF Server Start Script
#
# Relative path for accessing plugins
RP=../../../plugins
ECF=${RP}/org.eclipse.ecf_0.2.0/ecf.jar
UI=${RP}/org.eclipse.ecf.ui_0.2.0/ui.jar
SDO=${RP}/org.eclipse.ecf.sdo_0.2.0/ecf.sdo.jar
PROVIDER=${RP}/org.eclipse.ecf.provider_0.2.0/provider.jar
PRESENCE=${RP}/org.eclipse.ecf.presence_0.2.0/presence.jar
GED=${RP}/org.eclipse.ecf.example.sdo.gefeditor_0.2.0/editor.jar
ED=${RP}/org.eclipse.ecf.example.sdo.editor_0.2.0/editor.jar
LIBRARY=${RP}/org.eclipse.ecf.example.sdo.library_0.2.0/runtime/org.eclipse.ecf.example.library.jar
HELLO=${RP}/org.eclipse.ecf.example.hello_0.2.0/hello.jar
COLLAB=${RP}/org.eclipse.ecf.example.collab_0.2.0/client.jar

CP="../lib/core.jar:../lib/runtime.jar:../lib/osgi.jar:${ECF}:${UI}:${SDO}:${PROVIDER}:${PRESENCE}:${GED}:${ED}:${LIBRARY}:${HELLO}:${COLLAB}:."

TRACE="-Dorg.eclipse.ecf.Trace=true -Dorg.eclipse.ecf.provider.Trace=true" 

OPTIONS=

MAINCLASS=org.eclipse.ecf.provider.app.ServerApplication
ARGS=-c ../conf/server.xml $*

# Start server
echo "Starting server with options: ${OPTIONS} and args: ${ARGS}"
java -cp ${CP} ${OPTIONS} ${MAINCLASS} ${ARGS} 

