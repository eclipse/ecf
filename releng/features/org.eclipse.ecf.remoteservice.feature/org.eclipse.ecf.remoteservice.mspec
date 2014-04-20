<?xml version="1.0" encoding="UTF-8"?>
<md:mspec xmlns:md="http://www.eclipse.org/buckminster/MetaData-1.0" 
    name="org.eclipse.ecf.remoteservice" 
    materializer="p2" 
    url="org.eclipse.ecf.remoteservice.cquery">
    
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.remoteservice\.asyncproxy(\..+)?" installLocation="${targetPlatformPath}"/>
    
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.remoteservice(\..+)?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.remoteservice\.feature?" materializer="workspace"/>
    
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.tests\.remoteservice?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.tests\.remoteservice\.generic?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.tests\.osgi\.services\.distribution\.generic?" materializer="workspace"/>

    <md:mspecNode namePattern=".*" installLocation="${targetPlatformPath}"/>
</md:mspec>
	
