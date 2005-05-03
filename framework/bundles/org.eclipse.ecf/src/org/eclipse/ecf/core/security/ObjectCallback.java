package org.eclipse.ecf.core.security;

import javax.security.auth.callback.Callback;

public class ObjectCallback implements Callback {
	Object data;
	
	public ObjectCallback(Object val) {
		this.data = val;
	}
	
	public void setData(Object val) {
		this.data = val;
	}
	public Object getData() {
		return this.data;
	}
}
