# ECF Server Start Script
#
RP=../../../plugins
CURRENT=`pwd`
cd ${CURRENT}
# Relative path for accessing plugins
ECF=${RP}/org.eclipse.ecf_0.8.5/ecf.jar
UI=${RP}/org.eclipse.ecf.ui_0.8.5/ui.jar
SDO=${RP}/org.eclipse.ecf.sdo_0.8.5/ecf.sdo.jar
DS=${RP}/org.eclipse.ecf.datashare_0.8.5/datashare.jar
DSP=${RP}/org.eclipse.ecf.provider.datashare_0.8.5/dsprovider.jar
FS=${RP}/org.eclipse.ecf.fileshare_0.8.5/fileshare.jar
FSP=${RP}/org.eclipse.ecf.provider.fileshare_0.8.5/fsprovider.jar
PROVIDER=${RP}/org.eclipse.ecf.provider_0.8.5/provider.jar
PRESENCE=${RP}/org.eclipse.ecf.presence_0.8.5/presence.jar
GED=${RP}/org.eclipse.ecf.example.sdo.gefeditor_0.8.5/editor.jar
ED=${RP}/org.eclipse.ecf.example.sdo.editor_0.8.5/editor.jar
LIBRARY=${RP}/org.eclipse.ecf.example.sdo.library_0.8.5/runtime/org.eclipse.ecf.example.library.jar
DISCOVERY=${RP}/org.eclipse.ecf.discovery_0.8.5/discovery.jar
HELLO=${RP}/org.eclipse.ecf.example.hello_0.8.5/hello.jar
COLLAB=${RP}/org.eclipse.ecf.example.collab_0.8.5/client.jar
CED=${RP}/org.eclipse.ecf.example.collab.editor_0.8.5/collabeditor.jar

CP="../lib/core.jar:../lib/runtime.jar:../lib/osgi.jar:${ECF}:${UI}:${SDO}:${PROVIDER}:${PRESENCE}:${GED}:${ED}:${LIBRARY}:${DS}:${DSP}:${FS}:${FSP}:${HELLO}:${DISCOVERY}:${COLLAB}:${CED}:."

TRACE="-Dorg.eclipse.ecf.Trace -Dorg.eclipse.ecf.provider.Trace" 

OPTIONS=${TRACE}

MAINCLASS=org.eclipse.ecf.provider.app.ServerApplication
ARGS="-c ../conf/server.xml $*"

# Start server
echo "Starting server with options: ${OPTIONS} and args: ${ARGS}"
# java -cp ${CP} ${OPTIONS} ${MAINCLASS} ${ARGS} 
java -cp ${CP} ${OPTIONS} ${MAINCLASS} ${ARGS}
