<?xml version="1.0" encoding="UTF-8"?>
<md:mspec xmlns:md="http://www.eclipse.org/buckminster/MetaData-1.0" 
    name="org.eclipse.ecf.remoteservice.sdk.feature" 
    materializer="p2" 
    url="org.eclipse.ecf.remoteservice.sdk.feature.cquery">

    <md:mspecNode namePattern="^org\.eclipse\.ecf\.remoteservice\.asyncproxy(\..+)?" installLocation="${targetPlatformPath}"/>
    
    <md:mspecNode namePattern="^ch\.ethz\.iks(\..+)?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.eclipse\.team\.ecf(\..+)?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.eclipse\.ecf(\..+)?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.jivesoftware\.smack$" materializer="workspace"/>
    <md:mspecNode namePattern="^com\.mycorp\.examples(\..+)?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.eclipse\.osgi\.services\.remoteserviceadmin(\..+)?" materializer="workspace"/>

    <!-- commented out for sdk when comparing to platform
    <md:mspecNode namePattern=".*" installLocation="${target.location}"/>
    -->
    
    <md:mspecNode namePattern=".*" installLocation="${targetPlatformPath}"/>
</md:mspec>
	
