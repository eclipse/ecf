#!/bin/bash
JAVAPROG=java
GENERICHOSTNAME=localhost
GENERICPORT=3288
if [ "$#" -eq  "0" ];then
 echo "No arguments supplied. Usage:  rpimgmthost.sh <hostname> [<port]"
 exit 1
elif [ "$#" -eq "1" ]
then
   GENERICHOSTNAME=$1
   echo "Generic Server Hostname set to ${GENERICHOSTNAME}"
fi
JAVAPROPS="-Decf.generic.server.hostname=${GENERICHOSTNAME} -Decf.generic.server.port=${GENERICPORT} -Declipse.ignoreApp=true -Dosgi.noShutdown=true"
echo "javaprops=${JAVAPROPS}"
EQUINOXJAR=plugins/org.eclipse.osgi_3.10.0.v20140407-2102.jar
echo "equinoxjar=${EQUINOXJAR}"
ARGS="-configuration file:configuration -os linux -ws gtk -arch arm -console -consoleLog -debug"
echo "program args=${ARGS}"
echo "------------executing java-----------"
${JAVAPROG} ${JAVAPROPS} -jar ${EQUINOXJAR} ${ARGS}

