<?xml version="1.0" encoding="UTF-8"?>
<md:mspec xmlns:md="http://www.eclipse.org/buckminster/MetaData-1.0" 
    name="org.eclipse.ecf.zookeeper" 
    materializer="p2" 
    url="org.eclipse.ecf.zookeeper.cquery">
    
    <md:mspecNode namePattern="^org\.apache\.zookeeper(\..+)?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.provider\.zookeeper(\..+)?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.tests\.provider\.zookeeper(\..+)?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.discovery\.zookeeper\.feature?" materializer="workspace"/>

    <md:mspecNode namePattern=".*" installLocation="${targetPlatformPath}"/>
</md:mspec>
	
