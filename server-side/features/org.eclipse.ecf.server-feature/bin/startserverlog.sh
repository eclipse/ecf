# ECF Server Start Script
#
RP=../../../plugins
CURRENT=`pwd`
cd ${CURRENT}
# Relative path for accessing plugins
ECF=${RP}/org.eclipse.ecf_0.6.0/ecf.jar
UI=${RP}/org.eclipse.ecf.ui_0.6.0/ui.jar
SDO=${RP}/org.eclipse.ecf.sdo_0.6.0/ecf.sdo.jar
DS=${RP}/org.eclipse.ecf.datashare_0.6.0/datashare.jar
PROVIDER=${RP}/org.eclipse.ecf.provider_0.6.0/provider.jar
PRESENCE=${RP}/org.eclipse.ecf.presence_0.6.0/presence.jar
GED=${RP}/org.eclipse.ecf.example.sdo.gefeditor_0.6.0/editor.jar
ED=${RP}/org.eclipse.ecf.example.sdo.editor_0.6.0/editor.jar
LIBRARY=${RP}/org.eclipse.ecf.example.sdo.library_0.6.0/runtime/org.eclipse.ecf.example.library.jar
DISCOVERY=${RP}/org.eclipse.ecf.discovery_0.6.0/discovery.jar
HELLO=${RP}/org.eclipse.ecf.example.hello_0.6.0/hello.jar
COLLAB=${RP}/org.eclipse.ecf.example.collab_0.6.0/client.jar

CP="../lib/core.jar:../lib/runtime.jar:../lib/osgi.jar:${ECF}:${UI}:${SDO}:${PROVIDER}:${PRESENCE}:${GED}:${ED}:${LIBRARY}:${DS}:${HELLO}:${DISCOVERY}:${COLLAB}:."

TRACE="-Dorg.eclipse.ecf.Trace=true -Dorg.eclipse.ecf.provider.Trace=true"

OPTIONS=${TRACE}

MAINCLASS=org.eclipse.ecf.provider.app.ServerApplication
ARGS="-c ../conf/server.xml $*"

# Start server
echo "Starting server with options: ${OPTIONS} and args: ${ARGS}"
java -cp ${CP} ${OPTIONS} ${MAINCLASS} ${ARGS} > ../../../logs/3282/`date +%s`.log &

