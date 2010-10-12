<?xml version="1.0" encoding="UTF-8"?>
<md:mspec xmlns:md="http://www.eclipse.org/buckminster/MetaData-1.0" 
    name="org.eclipse.ecf" 
    materializer="p2" 
    url="org.eclipse.ecf.sdk.cquery">

	<!-- ECF discovery DNS-SD -->
	<md:mspecNode namePattern="^org\.eclipse\.ecf\.discovery\.feature?" materializer="p2" installLocation="${targetPlatformPath}"/>
	<md:mspecNode namePattern="^org\.eclipse\.ecf\.discovery\.dnssd(\..+)?" materializer="p2" installLocation="${targetPlatformPath}"/>
	<md:mspecNode namePattern="^org\.eclipse\.ecf\.provider\.dnssd(\..+)?" materializer="p2" installLocation="${targetPlatformPath}"/>
	<md:mspecNode namePattern="^org\.xbill\.dns?" materializer="p2" installLocation="${targetPlatformPath}"/>
    
    <md:mspecNode namePattern="^ch\.ethz\.iks(\..+)?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.eclipse\.team\.ecf(\..+)?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.eclipse\.ecf(\..+)?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.jivesoftware\.smack$" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.apache\.zookeeper(\..+)?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.json$" materializer="workspace"/>

    <!-- commented out for sdk when comparing to platform
    <md:mspecNode namePattern=".*" installLocation="${target.location}"/>
    -->
    
    <md:mspecNode namePattern=".*" installLocation="${targetPlatformPath}"/>
</md:mspec>
	
