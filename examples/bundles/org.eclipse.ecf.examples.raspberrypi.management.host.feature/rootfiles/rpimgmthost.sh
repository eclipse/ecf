#!/bin/bash
JAVAPROG=java
if [ "$#" -eq  "0" ];then
 echo "No arguments supplied. Usage:  rpimgmthost.sh hostname port"
 exit 1
fi
GENERICHOSTNAME="${1}"
if [ "${2}" != "" ];then
  GENERICPORT="${2}"
else
  GENERICPORT=3288
fi
echo "Hostname: ${GENERICHOSTNAME}"
echo "Port: ${GENERICPORT}"
JAVAPROPS="-Decf.generic.server.hostname=${GENERICHOSTNAME} -Decf.generic.server.port=${GENERICPORT} -Declipse.ignoreApp=true -Dosgi.noShutdown=true"
echo "javaprops=${JAVAPROPS}"
EQUINOXJAR=plugins/org.eclipse.osgi_3.10.0.v20140407-2102.jar
echo "equinox=${EQUINOXJAR}"
PROGARGS="-configuration file:configuration -os linux -ws gtk -arch arm -console -consoleLog -debug"
${JAVAPROG} ${JAVAPROPS} -jar ${EQUINOXJAR} ${PROGARGS}

