package org.eclipse.ecf.internal.remoteservices.ui;

import java.io.InvalidObjectException;

import org.eclipse.core.runtime.IConfigurationElement;

public class ServicesViewExtension {

	private final String viewId;
	private final boolean local;
	private int priority = 0;
	
	public ServicesViewExtension(IConfigurationElement ce) throws InvalidObjectException {
		this.viewId = ce.getAttribute("viewid");
		if (this.viewId == null) throw new InvalidObjectException("viewId must be set for services view extension");
		this.local = Boolean.parseBoolean(ce.getAttribute("local"));
		String priorityStr = ce.getAttribute("priority");
		try {
			this.priority = Integer.parseInt(priorityStr);
		} catch (NumberFormatException e) {
			// ignore
		}
	}
	
	public String getViewId() {
		return this.viewId;
	}
	
	public boolean isLocal() {
		return this.local;
	}
	
	public int getPriority() {
		return this.priority;
	}

	@Override
	public String toString() {
		return "ServicesViewExtension [viewId=" + viewId + ", local=" + local + ", priority=" + priority + "]";
	}
}
