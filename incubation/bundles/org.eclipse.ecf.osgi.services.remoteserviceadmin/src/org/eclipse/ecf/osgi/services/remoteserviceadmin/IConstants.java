package org.eclipse.ecf.osgi.services.remoteserviceadmin;

public interface IConstants {

	public static final String DISCOVERED_SERVICE_NAME = "osgiservice";
	
	// value of this property is expected to be of type ID
	public static final String CONTAINER_ID_PROPNAME = "ecf.containerid";
	public static final String CONTAINER_ID_NAMESPACE_PROPNAME = "ecf.containerid.namespace";
	
	// value of this property is expected to be of type Long
	public static final String REMOTE_SERVICE_ID_PROPNAME = "ecf.remoteserviceid";
	// value of this property is expected to be ID
	public static final String CONNECT_TARGET_ID_PROPNAME = "ecf.connecttargetid";
	public static final String CONNECT_TARGET_ID_NAMESPACE_PROPNAME = "ecf.connecttargetid.namespace";
	
	// value of this property is expected to be ID[]
	public static final String IDFILTER_PROPNAME = "ecf.idfilter";
	public static final String IDFILTER_NAMESPACE_PROPNAME = "ecf.idfilter.namespace";
	// value of this property is expected to be String
	public static final String REMOTESERVICE_FILTER_PROPNAME = "ecf.remoteservicefilter";

}
