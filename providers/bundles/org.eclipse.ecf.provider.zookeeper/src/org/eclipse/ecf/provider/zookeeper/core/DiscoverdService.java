/*******************************************************************************
 *  Copyright (c)2010 REMAIN B.V. The Netherlands. (http://www.remainsoftware.com).
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     Ahmed Aadel - initial API and implementation     
 *******************************************************************************/
package org.eclipse.ecf.provider.zookeeper.core;

import java.net.URI;
import java.util.Properties;
import java.util.UUID;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.discovery.ServiceInfo;
import org.eclipse.ecf.discovery.ServiceProperties;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.discovery.identity.ServiceIDFactory;
import org.eclipse.ecf.provider.zookeeper.core.internal.IService;
import org.eclipse.ecf.provider.zookeeper.core.internal.Localizer;
import org.eclipse.ecf.provider.zookeeper.core.internal.Notification;
import org.eclipse.ecf.provider.zookeeper.node.internal.INode;
import org.eclipse.ecf.provider.zookeeper.util.Geo;
import org.eclipse.ecf.provider.zookeeper.util.PrettyPrinter;
import org.osgi.framework.Constants;

/**
 * @author Ahmed Aadel
 * @since 0.1
 */

public class DiscoverdService extends ServiceInfo implements IService, INode {

	private static final long serialVersionUID = 3072424087109599612L;
	private Properties props = new Properties();
	private String uuid;
	private URI location;
	private IServiceTypeID serviceTypeID;

	public DiscoverdService(String path, Properties ps) {
		Assert.isNotNull(ps);
		this.uuid = path.split(INode._URI_)[0];
		this.props = ps;
		this.location = URI.create((String) this.props
				.remove(IService.LOCATION));
		super.priority = Integer.parseInt((String) this.props
				.remove(IService.PRIORITY));
		super.weight = Integer.parseInt((String) this.props
				.remove(IService.WEIGHT));
		String[] services = (String[]) this.props.remove(Constants.OBJECTCLASS);
		if (services == null) {
			services = (String[]) this.props
					.remove(INode.NODE_PROPERTY_SERVICES);
		}
		String na = (String) this.props.remove(INode.NODE_PROPERTY_NAME_NA);
		String[] protocols = (String[]) this.props
				.remove(INode.NODE_PROPERTY_NAME_PROTOCOLS);
		String[] scopes = (String[]) this.props
				.remove(INode.NODE_PROPERTY_NAME_SCOPE);
		super.properties = new ServiceProperties(this.props);
		this.serviceTypeID = ServiceIDFactory.getDefault().createServiceTypeID(
				ZooDiscoveryContainer.getSingleton().getConnectNamespace(),
				services, scopes, protocols, na);
		super.serviceID = new ZooDiscoveryServiceID(ZooDiscoveryContainer.getSingleton()
				.getConnectNamespace(), serviceTypeID, this.location);
	}

	public Properties getProperties() {
		return this.props;
	}

	public void dispose() {
		PrettyPrinter.prompt(PrettyPrinter.REMOTE_UNAVAILABLE, this);
		Localizer.getSingleton().localize(
				new Notification(this, Notification.UNAVAILABLE));
	}

	public String getNodeId() {
		return this.uuid;
	}

	public void regenerateNodeId() {
		this.uuid = UUID.randomUUID().toString();
	}

	public String getName() {
		return this.uuid;
	}

	public Namespace getNamespace() {
		return ZooDiscoveryContainer.getSingleton().getConnectNamespace();
	}

	public String toExternalForm() {
		return this.uuid;
	}

	public int compareTo(Object o) {
		Assert.isTrue(o != null && o instanceof DiscoverdService,
				"incompatible types for compare"); //$NON-NLS-1$
		return this.getServiceID().getName().compareTo(
				((DiscoverdService) o).getServiceID().getName());
	}

	public byte[] getPropertiesAsBytes() {
		return getPropertiesAsString().getBytes();
	}

	public String getPropertiesAsString() {
		String props = "";
		for (Object k : this.getProperties().keySet()) {
			props += k + "=" + this.getProperties().get(k) + "\n";//$NON-NLS-1$//$NON-NLS-2$
		}
		return props;
	}

	public String getPath() {
		return getServiceID().getName() + INode._URI_ + getLocation();
	}

	public String getAbsolutePath() {
		return INode.ROOT_SLASH + getPath();
	}

	public boolean isLocalNode() {
		return Geo.isLocal(getAbsolutePath());
	}

	public IService getWrappedService() {
		return this;
	}
}
