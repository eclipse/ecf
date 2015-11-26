package org.eclipse.ecf.internal.remoteservices.ui;

import java.io.InvalidObjectException;

import org.eclipse.core.runtime.IConfigurationElement;

public class ServicesViewExtension {

	private final String viewId;
	private final boolean local;
	private int priority = 0;

	public ServicesViewExtension(IConfigurationElement ce) throws InvalidObjectException {
		this.viewId = ce.getAttribute("viewid"); //$NON-NLS-1$
		if (this.viewId == null)
			throw new InvalidObjectException("viewId must be set for services view extension"); //$NON-NLS-1$
		this.local = Boolean.parseBoolean(ce.getAttribute("local")); //$NON-NLS-1$
		String priorityStr = ce.getAttribute("priority"); //$NON-NLS-1$
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
		return "ServicesViewExtension [viewId=" + viewId + ", local=" + local + ", priority=" + priority + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
}
