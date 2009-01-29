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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.hooks.service.EventHook;

public class EventHookImpl implements EventHook {

	private final static String[] EMPTY_STRING_ARRAY = new String[0];
	private final static String REMOTE_INTERFACES_WILDCARD = "*";
	public final static String ECF_RS_PROVIDER_CONFIGURATION = "org.eclipse.ecf";
	
	private final DistributionProviderImpl distributionProvider;
	
	private final Map remoteServiceReferences = new HashMap();
	
	public EventHookImpl(DistributionProviderImpl distributionProvider) {
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
		return sr.getProperty(RFC119ServiceInterfaceProperties.OSGI_REMOTE_INTERFACES_KEY);
	}
	
	private void handleRegisteredServiceEvent(
			ServiceReference serviceReference, Object source,
			Collection contexts) {
		traceEntering("handleRegisteredServiceEvent");
		Object remoteInterfaces = getRemoteInterfaces(serviceReference);
		Map ecfConfiguration = getECFConfiguration(serviceReference);
		if (remoteInterfaces != null && ecfConfiguration != null) {
			trace("handleRegisteredServiceEvent","serviceReference="+serviceReference+" has remoteInterfaces="+remoteInterfaces);
			String [] remotes = null;
			if (remoteInterfaces instanceof String[]) {
				remotes = getInterfacesForServiceReference((String[]) remoteInterfaces,serviceReference);
			}
			trace("handleRegisteredServiceEvent","serviceReference="+serviceReference+" has remotes="+Arrays.asList(remotes));
			if (remotes != null) {
				registerRemoteInterfaces(remotes,serviceReference,source, ecfConfiguration);
			}
		} else {
			trace("handleRegisteredServiceEvent","serviceReference="+serviceReference+" has no remoteInterfaces");
		}
		traceExiting("handleRegisteredServiceEvent");
	}

	private Map getECFConfiguration(ServiceReference serviceReference) {
		// Get property osgi.remote.configuration.type
		String[] remoteConfigurationType = (String []) serviceReference.getProperty(RFC119ServiceInterfaceProperties.OSGI_REMOTE_CONFIGURATION_TYPE);
		if (remoteConfigurationType != null && remoteConfigurationType[0].equals(ECF_RS_PROVIDER_CONFIGURATION)) {
			return parseECFConfigurationType(remoteConfigurationType);
		}
		return null;
	}

	private Map parseECFConfigurationType(String[] remoteConfigurationType) {
		Map results = new HashMap();
		// TODO parse ecf configuration from remoteConfigurationType
		return results;
	}

	private void registerRemoteInterfaces(String[] remotes,
			ServiceReference serviceReference, Object source, Map ecfConfiguration) {
		traceEntering("registerRemoteInterfaces");
		IRemoteServiceContainerAdapter ca = findAndChooseContainerAdapter(remotes,serviceReference,ecfConfiguration);
		if (ca == null) {
			trace("registerRemoteInterface","no container adapter found serviceReference="+serviceReference);
		} else {
			notifyRemoteServiceRegistered(serviceReference, ca.registerRemoteService(remotes, source, createPropertiesForRemoteService(remotes, serviceReference)));
		}
	}

	private void notifyRemoteServiceRegistered(ServiceReference serviceReference, IRemoteServiceRegistration registerRemoteService) {
		remoteServiceReferences.put(serviceReference, registerRemoteService);
		distributionProvider.addExposedService(serviceReference);
	}

	private IRemoteServiceContainerAdapter findAndChooseContainerAdapter(
			String[] remotes, ServiceReference serviceReference, Map ecfConfiguration) {
		IContainerManager containerManager = Activator.getDefault().getContainerManager();
		IContainer [] containers = containerManager.getAllContainers();
		
		// TODO Auto-generated method stub
		return null;
	}

	private Dictionary createPropertiesForRemoteService(String[] remotes,
			ServiceReference serviceReference) {
		// TODO Auto-generated method stub
		return null;
	}

	private String[] getInterfacesForServiceReference(
			String[] remoteInterfaces, ServiceReference serviceReference) {
		if (remoteInterfaces == null || remoteInterfaces.length == 0) return EMPTY_STRING_ARRAY;
		List results = new ArrayList();
		List interfaces = Arrays.asList((String []) serviceReference.getProperty(Constants.OBJECTCLASS));
		for(int i=0; i < remoteInterfaces.length; i++) {
			String intf = remoteInterfaces[i];
			if (REMOTE_INTERFACES_WILDCARD.equals(intf)) return (String []) interfaces.toArray(new String[] {});
			if (intf != null && interfaces.contains(intf)) {
				results.add(intf);
			}
		}
		return (String []) results.toArray(new String [] {});
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
