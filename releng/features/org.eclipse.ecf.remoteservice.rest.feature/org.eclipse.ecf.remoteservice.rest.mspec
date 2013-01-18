<?xml version="1.0" encoding="UTF-8"?>
<md:mspec xmlns:md="http://www.eclipse.org/buckminster/MetaData-1.0" 
    name="org.eclipse.ecf.remoteservice.rest" 
    materializer="p2" 
    url="org.eclipse.ecf.remoteservice.rest.cquery">
    
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.remoteservice\.rest?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.remoteservice\.rest\.feature?" materializer="workspace"/>
    
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.tests\.apache\.httpclient\.server?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.tests\.remoteservice\.rest?" materializer="workspace"/>

    <md:mspecNode namePattern=".*" installLocation="${targetPlatformPath}"/>
</md:mspec>
	
