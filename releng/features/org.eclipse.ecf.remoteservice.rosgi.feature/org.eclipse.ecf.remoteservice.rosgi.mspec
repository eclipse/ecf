<?xml version="1.0" encoding="UTF-8"?>
<md:mspec xmlns:md="http://www.eclipse.org/buckminster/MetaData-1.0" 
    name="org.eclipse.ecf.remoteservice.rosgi" 
    materializer="p2" 
    url="org.eclipse.ecf.remoteservice.rosgi.cquery">
    
    <md:mspecNode namePattern="^ch\.ethz\.iks\.r_osgi\.remote?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.provider\.r_osgi?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.remoteservice\.rosgi\.feature?" materializer="workspace"/>
    
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.tests\.remoteservice?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.tests\.remoteservice\.r-osgi?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.tests\.osgi\.services\.distribution\.r-osgi?" materializer="workspace"/>

    <md:mspecNode namePattern=".*" installLocation="${targetPlatformPath}"/>
</md:mspec>
	
