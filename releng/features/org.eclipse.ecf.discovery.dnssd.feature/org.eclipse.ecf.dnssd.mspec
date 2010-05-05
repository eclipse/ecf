<?xml version="1.0" encoding="UTF-8"?>
<md:mspec xmlns:md="http://www.eclipse.org/buckminster/MetaData-1.0" 
    name="org.eclipse.ecf.dnssd" 
    materializer="p2" 
    url="org.eclipse.ecf.dnssd.cquery">
    
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.provider\.dnssd(\..+)?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.discovery\.dnssd\.feature?" materializer="workspace"/>
    
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.tests\.provider\.dnssd?" materializer="workspace"/>

    <md:mspecNode namePattern=".*" installLocation="${targetPlatformPath}"/>
</md:mspec>
	
