package org.eclipse.ecf.osgi.services.remoteserviceadmin;

public class RemoteConstants {

	public static final String SERVICE_TYPE = "osgirsvc";
	
	public static final String DISCOVERY_SCOPE = "ecf.endpoint.discovery.scope";
	public static final String DISCOVERY_PROTOCOLS = "ecf.endpoint.discovery.protocols";
	public static final String DISCOVERY_NAMING_AUTHORITY = "ecf.endpoint.discovery.na";
	public static final String DISCOVERY_SERVICE_NAME = "ecf.endpoint.discovery.servicename";
	public static final String DISCOVERY_DEFAULT_SERVICE_NAME_PREFIX = "osgirsvc_";

	// value of this property is expected to be of type ID
	public static final String CONTAINER_ID_PROPNAME = "ecf.endpoint.containerid";
	public static final String CONTAINER_ID_NAMESPACE_PROPNAME = "ecf.endpoint.containerid.namespace";
	// value of this property is expected to be of type Long
	public static final String REMOTE_SERVICE_ID_PROPNAME = "ecf.endpoint.remoteserviceid";
	
	
	// value of this property is expected to be ID
	public static final String CONNECT_TARGET_ID_PROPNAME = "ecf.endpoint.connecttargetid";
	public static final String CONNECT_TARGET_ID_NAMESPACE_PROPNAME = "ecf.endpoint.connecttargetid.namespace";
	
	// value of this property is expected to be ID[]
	public static final String IDFILTER_PROPNAME = "ecf.endpoint.idfilter";
	public static final String IDFILTER_NAMESPACE_PROPNAME = "ecf.endpoint.idfilter.namespace";
	// value of this property is expected to be String
	public static final String REMOTESERVICE_FILTER_PROPNAME = "ecf.endpoint.remoteservicefilter";

}
