<?xml version="1.0" encoding="UTF-8"?>
<md:mspec xmlns:md="http://www.eclipse.org/buckminster/MetaData-1.0" 
    name="org.eclipse.ecf.jmdns" 
    materializer="p2" 
    url="org.eclipse.ecf.jmdns.cquery">
    
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.provider\.jmdns(\..+)?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.discovery\.jmdns\.feature?" materializer="workspace"/>
    
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.tests\.provider\.jmdns?" materializer="workspace"/>

    <md:mspecNode namePattern=".*" installLocation="${targetPlatformPath}"/>
</md:mspec>
	
