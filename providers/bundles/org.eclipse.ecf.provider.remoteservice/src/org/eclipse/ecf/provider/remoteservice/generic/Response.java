package org.eclipse.ecf.provider.remoteservice.generic;

import java.io.Serializable;

public class Response implements Serializable {

	private static final long serialVersionUID = 634820397523983872L;
	long requestId;
	Object response;
	
	public Response(long requestId, Object response) {
		this.requestId = requestId;
		this.response = response;
	}

	public long getRequestId() {
		return requestId;
	}
	
	public Object getResponse() {
		return response;
	}
}
