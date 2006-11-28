# ECF Server Start Script
# location of plugins relative to this script
RP=../../../plugins
CURRENT=`pwd`
cd ${CURRENT}
# Relative path for accessing core ECF plugins
CALL=${RP}/org.eclipse.ecf.call_0.9.4.jar
DS=${RP}/org.eclipse.ecf.datashare_0.9.4.jar
DISCOVERY=${RP}/org.eclipse.ecf.discovery_0.9.4.jar
DOC=${RP}/org.eclipse.ecf.doc_0.9.4.jar
FT=${RP}/org.eclipse.ecf.filetransfer_0.9.4.jar
IDENTITY=${RP}/org.eclipse.ecf.identity_0.9.4.jar
PRESENCE=${RP}/org.eclipse.ecf.presence_0.9.4.jar
DSP=${RP}/org.eclipse.ecf.provider.datashare_0.9.4.jar
FTP=${RP}/org.eclipse.ecf.provider.filetransfer_0.9.4.jar
IRCP=${RP}/org.eclipse.ecf.provider.irc_0.9.4.jar
JMDNS=${RP}/org.eclipse.ecf.provider.jmdns_0.9.4.jar
RSP=${RP}/org.eclipse.ecf.provider.remoteservice_0.9.4.jar
UIP=${RP}/org.eclipse.ecf.provider.ui_0.9.4.jar
XMPPP=${RP}/org.eclipse.ecf.provider.xmpp_0.9.4.jar
PROVIDER=${RP}/org.eclipse.ecf.provider_0.9.4.jar
RS=${RP}/org.eclipse.ecf.remoteservice_0.9.4.jar
SERVER=${RP}/org.eclipse.ecf.server_0.9.4.jar
SO=${RP}/org.eclipse.ecf.sharedobject_0.9.4.jar
UI=${RP}/org.eclipse.ecf.ui_0.9.4.jar
ECF=${RP}/org.eclipse.ecf_0.9.4.jar
SMACK=${RP}/org.jivesoftware.smack_2.2.0.jar
# Now all together
CORE="${CALL}:${DS}:${DISCOVERY}:${DOC}:${FT}:${IDENTITY}:${PRESENCE}:${DSP}:${FTP}:${IRCP}:${JMDNS}:${RSP}:${UIP}:${XMPPP}:${PROVIDER}:${RS}:${SERVER}:${SO}:${UI}:${ECF}:${SMACK}"

# examples plugins
CLIENTS=${RP}/org.eclipse.ecf.example.clients_0.9.4.jar
CED=${RP}/org.eclipse.ecf.example.collab.editor_0.9.4.jar
COLLAB=${RP}/org.eclipse.ecf.example.collab_0.9.4.jar
# now all together
EXAMPLES="${CLIENTS}:${CED}:${COLLAB}"
# Eclipse 3.2 runtime classes
RUNTIME="../lib/org.eclipse.equinox.registry_3.2.100.v20061023.jar:../lib/org.eclipse.core.runtime_3.2.100.v20061030.jar:../lib/org.eclipse.equinox.common_3.3.0.v20061023.jar:../lib/org.eclipse.osgi_3.3.0.v20061101.jar"
# Entire classpath together
CP="${RUNTIME}:.:${CORE}:${EXAMPLES}"

MAINCLASS=org.eclipse.ecf.provider.app.ServerApplication
ARGS="-c ../conf/server.xml $*"

# Start server
echo "Starting server"
echo "Main class: ${MAINCLASS}"
echo "Classpath: ${CP}"
echo "Args: ${ARGS}"
# start main class with classpath and args
java -cp ${CP} ${MAINCLASS} ${ARGS} 


