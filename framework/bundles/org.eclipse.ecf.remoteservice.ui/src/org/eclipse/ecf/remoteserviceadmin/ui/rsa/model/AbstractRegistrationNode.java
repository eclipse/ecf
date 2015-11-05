/*******************************************************************************
 * Copyright (c) 2015 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Scott Lewis - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.remoteserviceadmin.ui.rsa.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ecf.internal.remoteservices.ui.Messages;
import org.eclipse.ecf.remoteserviceadmin.ui.endpoint.model.EndpointPropertySource;
import org.eclipse.ui.views.properties.IPropertySource;
import org.osgi.framework.ServiceReference;

/**
 * @since 3.3
 */
public abstract class AbstractRegistrationNode extends AbstractRSANode {

	public static final String ERROR = Messages.AbstractRegistrationNode_ErrorName;

	private final boolean error;

	public AbstractRegistrationNode(Throwable t) {
		this.error = t != null;
	}

	protected boolean hasError() {
		return error;
	}

	public abstract boolean isClosed();

	protected abstract String getValidName();

	public abstract ServiceReference getServiceReference();

	public String getName() {
		return hasError() ? ERROR : (isClosed() ? CLOSED : getValidName());
	}

	protected Map<String, Object> convertServicePropsToMap(ServiceReference sr) {
		String[] keys = sr.getPropertyKeys();
		Map<String, Object> result = new HashMap<String, Object>();
		for (String key : keys)
			result.put(key, sr.getProperty(key));
		return result;
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter == IPropertySource.class) {
			ServiceReference sr = getServiceReference();
			if (sr != null)
				return new EndpointPropertySource(convertServicePropsToMap(sr));
		}
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	public abstract void close();

}
