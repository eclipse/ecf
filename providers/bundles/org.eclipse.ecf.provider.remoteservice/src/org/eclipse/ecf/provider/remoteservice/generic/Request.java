package org.eclipse.ecf.provider.remoteservice.generic;

import java.io.Serializable;

import org.eclipse.ecf.core.identity.ID;

public class Request implements Serializable {

	private static final long serialVersionUID = -6428866228973362178L;

	private static long nextRequestId = 0;
	long requestId;
	
	ID requestContainerID;
	
	long serviceId;
	
	RemoteCallImpl call;
	
	public Request(ID requestContainerID, long serviceId, RemoteCallImpl call) {
		this.requestContainerID = requestContainerID;
		this.serviceId = serviceId;
		this.call = call;
		this.requestId = nextRequestId++;
	}
	
	public long getRequestId() {
		return requestId;
	}
	
	public ID getRequestContainerID() {
		return requestContainerID;
	}
	
	public long getServiceId() {
		return serviceId;
	}
	
	public RemoteCallImpl getCall() {
		return call;
	}
	public String toString() {
		StringBuffer buf = new StringBuffer("RemoteCallRequest[");
		buf.append("svcid=").append(serviceId).append(";")
				.append("from=").append(requestContainerID).append(";").append(call).append(";").append(
						"requestid=").append(requestId).append("]");
		return buf.toString();
	}
}
