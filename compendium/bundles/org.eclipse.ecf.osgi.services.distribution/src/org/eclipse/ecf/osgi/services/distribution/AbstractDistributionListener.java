/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.distribution;

import java.util.Arrays;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.internal.osgi.services.distribution.Activator;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

public abstract class AbstractDistributionListener {

	protected int logLevel = LogService.LOG_INFO;

	public AbstractDistributionListener(int logLevel) {
		this.logLevel = logLevel;
	}

	public AbstractDistributionListener() {
	}

	protected int getLogLevel() {
		return logLevel;
	}

	protected void log(ServiceReference serviceReference, String message,
			Throwable t) {
		Activator a = Activator.getDefault();
		if (a == null)
			return;
		a.log(serviceReference, getLogLevel(), message, t);
	}

	protected String createTabs(int tabLevel) {
		StringBuffer sb = new StringBuffer(""); //$NON-NLS-1$
		for (int i = 0; i < tabLevel; i++) {
			sb.append("\t"); //$NON-NLS-1$
		}
		return sb.toString();
	}

	protected String printID(int tabLevel, String label, ID id) {
		StringBuffer sb = new StringBuffer();
		sb.append(createTabs(tabLevel)).append(label).append("="); //$NON-NLS-1$
		sb.append((id == null) ? "null" : id.toExternalForm()); //$NON-NLS-1$
		return sb.toString();
	}

	protected String printRemoteServiceRegistration(int tabLevel,
			IRemoteServiceRegistration remoteRegistration) {
		StringBuffer sb = new StringBuffer();
		if (remoteRegistration != null) {
			sb.append(printID(tabLevel,
					"remoteServiceID", remoteRegistration.getID())); //$NON-NLS-1$
			sb.append("\n"); //$NON-NLS-1$
			sb.append(createTabs(tabLevel))
					.append("properties") //$NON-NLS-1$
					.append("\n") //$NON-NLS-1$
					.append(printRemoteServiceReferenceProperties(tabLevel + 1,
							remoteRegistration.getReference())).append("\n"); //$NON-NLS-1$
		}
		return sb.toString();
	}

	protected String printRemoteServiceReference(int tabLevel,
			IRemoteServiceReference remoteReference) {
		StringBuffer sb = new StringBuffer();
		if (remoteReference != null) {
			sb.append(printID(tabLevel,
					"remoteServiceID", remoteReference.getID())); //$NON-NLS-1$
			sb.append("\n"); //$NON-NLS-1$
			sb.append(createTabs(tabLevel))
					.append("properties") //$NON-NLS-1$
					.append("\n") //$NON-NLS-1$
					.append(printRemoteServiceReferenceProperties(tabLevel + 1,
							remoteReference)).append("\n"); //$NON-NLS-1$
		}
		return sb.toString();
	}

	protected String printRemoteServiceReferenceProperties(int tabLevel,
			IRemoteServiceReference remoteReference) {
		StringBuffer sb = new StringBuffer();
		if (remoteReference != null) {
			String[] propKeys = remoteReference.getPropertyKeys();
			for (int i = 0; i < propKeys.length; i++) {
				if (i > 0)
					sb.append("\n"); //$NON-NLS-1$
				sb.append(createTabs(tabLevel))
						.append("name=").append(propKeys[i]); //$NON-NLS-1$
				Object value = remoteReference.getProperty(propKeys[i]);
				sb.append(";value["); //$NON-NLS-1$
				Class valueClass = value.getClass();
				String classStr = ""; //$NON-NLS-1$
				String valueStr = ""; //$NON-NLS-1$
				if (valueClass.isArray()) {
					classStr = valueClass.getComponentType().getName() + "[]"; //$NON-NLS-1$
					valueStr = Arrays.asList((Object[]) value).toString();
				} else {
					classStr = valueClass.getName();
					valueStr = value.toString();
				}
				sb.append(classStr).append("]").append("=").append(valueStr); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return sb.toString();
	}

	protected String printRemoteServiceContainer(int tabLevel,
			IRemoteServiceContainer remoteServiceContainer) {
		StringBuffer sb = new StringBuffer();
		IContainer container = remoteServiceContainer.getContainer();
		if (container != null) {
			sb.append(printID(tabLevel, "ID", container.getID())); //$NON-NLS-1$
			sb.append("\n"); //$NON-NLS-1$
			sb.append(printID(tabLevel,
					"connectedID", container.getConnectedID())); //$NON-NLS-1$
		}
		return sb.toString();
	}

}
