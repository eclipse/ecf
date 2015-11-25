/*******************************************************************************
 * Copyright (c) 2015 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Scott Lewis - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.remoteserviceadmin.ui.service.model;

import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.views.properties.IPropertySource;
import org.osgi.framework.Constants;

public class ServiceNode extends AbstractServicesNode {

	private final long bundleId;
	private final long[] usingBundleIds;
	private Map<String, Object> properties;

	public ServiceNode(long bundleId, long[] usingBundles, Map<String, Object> props) {
		this.bundleId = bundleId;
		this.usingBundleIds = usingBundles;
		this.properties = props;
	}

	public void setProperties(Map<String, Object> updatedProperties) {
		this.properties = updatedProperties;
	}

	public long getServiceId() {
		return (Long) getProperties().get(Constants.SERVICE_ID);
	}

	public long getBundleId() {
		return this.bundleId;
	}

	public long[] getUsingBundleIds() {
		return this.usingBundleIds;
	}

	public Map<String, Object> getProperties() {
		return this.properties;
	}

	public String[] getServiceInterfaces() {
		return (String[]) this.properties.get(Constants.OBJECTCLASS);
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter == IPropertySource.class)
			return new ServicePropertySource(getProperties());
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

}
