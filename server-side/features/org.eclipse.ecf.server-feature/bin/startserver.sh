# ECF Server Start Script
#
RP=../../../plugins
CURRENT=`pwd`
cd ${CURRENT}
# Relative path for accessing plugins
ECF=${RP}/org.eclipse.ecf_0.5.4/ecf.jar
UI=${RP}/org.eclipse.ecf.ui_0.5.4/ui.jar
SDO=${RP}/org.eclipse.ecf.sdo_0.5.4/ecf.sdo.jar
DS=${RP}/org.eclipse.ecf.datashare_0.5.4/datashare.jar
PROVIDER=${RP}/org.eclipse.ecf.provider_0.5.4/provider.jar
PRESENCE=${RP}/org.eclipse.ecf.presence_0.5.4/presence.jar
GED=${RP}/org.eclipse.ecf.example.sdo.gefeditor_0.5.4/editor.jar
ED=${RP}/org.eclipse.ecf.example.sdo.editor_0.5.4/editor.jar
LIBRARY=${RP}/org.eclipse.ecf.example.sdo.library_0.5.4/runtime/org.eclipse.ecf.example.library.jar
DISCOVERY=${RP}/org.eclipse.ecf.discovery_0.5.4/discovery.jar
HELLO=${RP}/org.eclipse.ecf.example.hello_0.5.4/hello.jar
COLLAB=${RP}/org.eclipse.ecf.example.collab_0.5.4/client.jar

CP="../lib/core.jar:../lib/runtime.jar:../lib/osgi.jar:${ECF}:${UI}:${SDO}:${PROVIDER}:${PRESENCE}:${GED}:${ED}:${LIBRARY}:${DS}:${HELLO}:${DISCOVERY}:${COLLAB}:."

TRACE="-Dorg.eclipse.ecf.Trace=true -Dorg.eclipse.ecf.provider.Trace=true" 

OPTIONS=

MAINCLASS=org.eclipse.ecf.provider.app.ServerApplication
ARGS="-c ../conf/server.xml $*"

# Start server
echo "Starting server with options: ${OPTIONS} and args: ${ARGS}"
# java -cp ${CP} ${OPTIONS} ${MAINCLASS} ${ARGS} 
java -cp ${CP} ${OPTIONS} ${MAINCLASS} ${ARGS}
