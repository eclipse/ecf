<?xml version="1.0" encoding="UTF-8"?>
<md:mspec xmlns:md="http://www.eclipse.org/buckminster/MetaData-1.0" 
    name="org.eclipse.ecf.jslp" 
    materializer="p2" 
    url="org.eclipse.ecf.jslp.cquery">
    
    <md:mspecNode namePattern="^ch\.ethz\.iks\.slp?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.provider\.jslp?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.discovery\.slp\.feature?" materializer="workspace"/>
    
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.tests\.provider\.jslp?" materializer="workspace"/>

    <md:mspecNode namePattern=".*" installLocation="${targetPlatformPath}"/>
</md:mspec>
	
