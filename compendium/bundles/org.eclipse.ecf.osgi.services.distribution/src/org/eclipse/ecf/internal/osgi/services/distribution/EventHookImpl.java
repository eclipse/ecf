/*******************************************************************************
* Copyright (c) 2009 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.distribution;

import java.util.Collection;

import org.eclipse.ecf.core.util.Trace;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.hooks.service.EventHook;

public class EventHookImpl implements EventHook {

	private final ECFRSDistributionProvider distributionProvider;
	
	public EventHookImpl(ECFRSDistributionProvider distributionProvider) {
		this.distributionProvider = distributionProvider;
	}

	public void event(ServiceEvent event, Collection contexts) {
		traceEntering("event");
		switch (event.getType()) {
		case ServiceEvent.MODIFIED:
			handleModifiedServiceEvent(event.getServiceReference(),event.getSource(),contexts);
			break;
		case ServiceEvent.MODIFIED_ENDMATCH:
			break;
		case ServiceEvent.REGISTERED:
			handleRegisteredServiceEvent(event.getServiceReference(),event.getSource(),contexts);
			break;
		case ServiceEvent.UNREGISTERING:
			handleUnregisteringServiceEvent(event.getServiceReference(),event.getSource(),contexts);
			break;
		default:
			break;
		}
		traceExiting("event");
	}

	private void handleUnregisteringServiceEvent(
			ServiceReference serviceReference, Object source,
			Collection contexts) {
		traceEntering("handleUnregisteringServiceEvent");
		// TODO
		traceExiting("handleUnregisteringServiceEvent");
	}

	Object getRemoteInterfaces(ServiceReference sr) {
		return sr.getProperty(ServiceInterfaceConstants.OSGI_REMOTE_INTERFACES_KEY);
	}
	
	private void handleRegisteredServiceEvent(
			ServiceReference serviceReference, Object source,
			Collection contexts) {
		traceEntering("handleRegisteredServiceEvent");
		Object remoteInterfaces = getRemoteInterfaces(serviceReference);
		if (remoteInterfaces != null) {
			trace("handleRegisteredServiceEvent","serviceReference="+serviceReference+" has remoteInterfaces="+remoteInterfaces);
			// XXX todo
			
		} else {
			trace("handleRegisteredServiceEvent","serviceReference="+serviceReference+" has no remoteInterfaces");
		}
		traceExiting("handleRegisteredServiceEvent");
	}

	private void handleModifiedServiceEvent(ServiceReference serviceReference,
			Object source, Collection contexts) {
		traceEntering("handleModifiedServiceEvent");
		// TODO
		traceExiting("handleModifiedServiceEvent");
	}

	
	private void traceEntering(String methodName) {
		Trace.entering(Activator.PLUGIN_ID, DebugOptions.METHODS_ENTERING, this.getClass(), methodName);
	}
	
	private void traceExiting(String methodName) {
		Trace.exiting(Activator.PLUGIN_ID, DebugOptions.METHODS_EXITING, this.getClass(), "handleUnregisteringServiceEvent");		
	}

	private void trace(String methodName, String message) {
		Trace.trace(Activator.PLUGIN_ID, DebugOptions.DEBUG, this.getClass(), methodName, message);
	}
	
}
