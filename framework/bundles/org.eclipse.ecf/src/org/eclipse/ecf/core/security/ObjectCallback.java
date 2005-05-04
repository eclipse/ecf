package org.eclipse.ecf.core.security;

import javax.security.auth.callback.Callback;

public class ObjectCallback implements Callback {
	Object data;
	
	public ObjectCallback() {
		data = null;
	}
	public ObjectCallback(Object val) {
		this.data = val;
	}
	
	public void setObject(Object val) {
		this.data = val;
	}
	public Object getObject() {
		return this.data;
	}
}
