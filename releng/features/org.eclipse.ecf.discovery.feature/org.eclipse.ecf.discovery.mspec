<?xml version="1.0" encoding="UTF-8"?>
<md:mspec xmlns:md="http://www.eclipse.org/buckminster/MetaData-1.0" 
    name="org.eclipse.ecf.discovery" 
    materializer="p2" 
    url="org.eclipse.ecf.discovery.cquery">
    
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.provider\.discovery(\..+)?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.discovery(\..+)?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.discovery\.feature?" materializer="workspace"/>
    
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.tests\.provider\.discovery?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.tests\.discovery?" materializer="workspace"/>

    <md:mspecNode namePattern=".*" installLocation="${targetPlatformPath}"/>
</md:mspec>
	
