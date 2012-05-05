<?xml version="1.0" encoding="UTF-8"?>
<md:mspec xmlns:md="http://www.eclipse.org/buckminster/MetaData-1.0" 
    name="org.eclipse.ecf" 
    materializer="p2" 
    url="org.eclipse.ecf.tests.cquery">
    
    <md:mspecNode namePattern="^org\.eclipse\.osgi\.services\.remoteserviceadmin(\..+)?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.filetransfer?" installLocation="${targetPlatformPath}"/>
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.tests(\..+)?" materializer="workspace"/>
    
    <md:mspecNode namePattern=".*" installLocation="${targetPlatformPath}"/>
</md:mspec>
	
