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
	
	Response response;
	
	boolean done = false;
	
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
	public void setResponse(Response response) {
		this.response = response;
	}
	public Response getResponse() {
		return response;
	}
	public boolean isDone() {
		return done;
	}
	public void setDone(boolean val) {
		this.done = val;
	}
	public String toString() {
		StringBuffer buf = new StringBuffer("Request[");
		buf.append("requestId=").append(requestId).append(";cont=").append(
				requestContainerID).append(";serviceId=").append(serviceId)
				.append(";call=").append(call).append(";done=").append(done)
				.append(";response=").append(response).append("]");
		return buf.toString();
	}

}
