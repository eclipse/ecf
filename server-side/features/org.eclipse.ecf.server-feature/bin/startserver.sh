# ECF Server Start Script
#
RP=../../../plugins
CURRENT=`pwd`
cd ${CURRENT}
# Relative path for accessing plugins
ECF=${RP}/org.eclipse.ecf_0.8.2/ecf.jar
UI=${RP}/org.eclipse.ecf.ui_0.8.2/ui.jar
SDO=${RP}/org.eclipse.ecf.sdo_0.8.2/ecf.sdo.jar
DS=${RP}/org.eclipse.ecf.datashare_0.8.2/datashare.jar
DSP=${RP}/org.eclipse.ecf.provider.datashare_0.8.2/dsprovider.jar
FS=${RP}/org.eclipse.ecf.fileshare_0.8.2/fileshare.jar
FSP=${RP}/org.eclipse.ecf.provider.fileshare_0.8.2/fsprovider.jar
PROVIDER=${RP}/org.eclipse.ecf.provider_0.8.2/provider.jar
PRESENCE=${RP}/org.eclipse.ecf.presence_0.8.2/presence.jar
GED=${RP}/org.eclipse.ecf.example.sdo.gefeditor_0.8.2/editor.jar
ED=${RP}/org.eclipse.ecf.example.sdo.editor_0.8.2/editor.jar
LIBRARY=${RP}/org.eclipse.ecf.example.sdo.library_0.8.2/runtime/org.eclipse.ecf.example.library.jar
DISCOVERY=${RP}/org.eclipse.ecf.discovery_0.8.2/discovery.jar
HELLO=${RP}/org.eclipse.ecf.example.hello_0.8.2/hello.jar
COLLAB=${RP}/org.eclipse.ecf.example.collab_0.8.2/client.jar
CED=${RP}/org.eclipse.ecf.example.collab.editor_0.8.2/collabeditor.jar

CP="../lib/core.jar:../lib/runtime.jar:../lib/osgi.jar:${ECF}:${UI}:${SDO}:${PROVIDER}:${PRESENCE}:${GED}:${ED}:${LIBRARY}:${DS}:${DSP}:${FS}:${FSP}:${HELLO}:${DISCOVERY}:${COLLAB}:${CED}:."

TRACE="-Dorg.eclipse.ecf.Trace -Dorg.eclipse.ecf.provider.Trace" 

OPTIONS=${TRACE}

MAINCLASS=org.eclipse.ecf.provider.app.ServerApplication
ARGS="-c ../conf/server.xml $*"

# Start server
echo "Starting server with options: ${OPTIONS} and args: ${ARGS}"
# java -cp ${CP} ${OPTIONS} ${MAINCLASS} ${ARGS} 
java -cp ${CP} ${OPTIONS} ${MAINCLASS} ${ARGS}
