/*******************************************************************************
 * Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.distribution;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.internal.osgi.services.distribution.Activator;
import org.eclipse.ecf.internal.osgi.services.distribution.DebugOptions;
import org.eclipse.ecf.internal.osgi.services.distribution.LogUtility;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.RemoteServiceContainer;

/**
 * Abstract superclass for IHostContainerFinders and IProxyContainerFinders.
 * 
 */
public abstract class AbstractContainerFinder {

	public static final IRemoteServiceContainer[] EMPTY_REMOTE_SERVICE_CONTAINER_ARRAY = new IRemoteServiceContainer[] {};

	protected IContainer[] getAllContainers() {
		Activator activator = Activator.getDefault();
		if (activator == null)
			return null;
		IContainerManager containerManager = activator.getContainerManager();
		if (containerManager == null)
			return null;
		return containerManager.getAllContainers();
	}

	protected IRemoteServiceContainerAdapter hasRemoteServiceContainerAdapter(
			IContainer container) {
		return (IRemoteServiceContainerAdapter) container
				.getAdapter(IRemoteServiceContainerAdapter.class);
	}

	protected IRemoteServiceContainer[] getRemoteServiceContainers(
			IContainer[] containers) {
		List results = new ArrayList();
		for (int i = 0; i < containers.length; i++) {
			IRemoteServiceContainerAdapter adapter = hasRemoteServiceContainerAdapter(containers[i]);
			if (adapter != null)
				results.add(new RemoteServiceContainer(containers[i], adapter));
		}
		return (IRemoteServiceContainer[]) results
				.toArray(new IRemoteServiceContainer[] {});
	}

	protected boolean includeContainerWithConnectNamespace(
			IContainer container, String connectNamespaceName) {
		if (connectNamespaceName != null) {
			Namespace namespace = container.getConnectNamespace();
			if (namespace != null
					&& namespace.getName().equals(connectNamespaceName))
				return true;
		}
		return false;
	}

	protected void connectContainer(IContainer container, ID connectTargetID,
			IConnectContext connectContext) throws ContainerConnectException {
		trace("connectContainer", "Connecting container=" + container.getID()
				+ " to connectTargetID=" + connectTargetID);
		container.connect(connectTargetID, connectContext);
	}

	protected void trace(String methodName, String message) {
		LogUtility.trace(methodName, DebugOptions.CONTAINERFINDER, this
				.getClass(), message);
	}

	protected void traceException(String methodName, String message, Throwable t) {
		LogUtility.traceException(methodName, DebugOptions.EXCEPTIONS_CATCHING,
				this.getClass(), message, t);
	}

	protected void logError(String methodName, String message, Throwable t) {
		LogUtility.logError(methodName, DebugOptions.CONTAINERFINDER, this
				.getClass(), message, t);
	}

	protected void logError(String methodName, String message) {
		LogUtility.logError(methodName, DebugOptions.CONTAINERFINDER, this
				.getClass(), message);
	}

	protected void logWarning(String methodName, String message) {
		LogUtility.logWarning(methodName, DebugOptions.CONTAINERFINDER, this
				.getClass(), message);
	}

}
