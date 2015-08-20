/*******************************************************************************
 * Copyright (c) 2015 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.remoteservice.provider;

import java.util.Dictionary;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.provider.IContainerInstantiator;

/**
 * Basic implementation of IRemoteServiceDistributionProvider.  Intended to be subclassed by distribution
 * provider implementations.
 * 
 * @since 8.7
 */
public class RemoteServiceDistributionProvider implements IRemoteServiceDistributionProvider {

	private String name;
	private IContainerInstantiator instantiator;
	private String description;
	private boolean server;
	private boolean hidden;

	protected RemoteServiceDistributionProvider() {

	}

	protected RemoteServiceDistributionProvider(String name, IContainerInstantiator instantiator) {
		setName(name).setInstantiator(instantiator);
	}

	protected RemoteServiceDistributionProvider(String name, IContainerInstantiator instantiator, String description) {
		setName(name).setInstantiator(instantiator).setDescription(description);
	}

	protected RemoteServiceDistributionProvider(String name, IContainerInstantiator instantiator, String description, boolean server) {
		setName(name).setInstantiator(instantiator).setDescription(description).setServer(server);
	}

	protected String getName() {
		return this.name;
	}

	protected RemoteServiceDistributionProvider setName(String name) {
		Assert.isNotNull(name);
		this.name = name;
		return this;
	}

	protected IContainerInstantiator getInstantiator() {
		return instantiator;
	}

	protected RemoteServiceDistributionProvider setInstantiator(IContainerInstantiator instantiator) {
		Assert.isNotNull(instantiator);
		this.instantiator = instantiator;
		return this;
	}

	protected String getDescription() {
		return this.description;
	}

	protected RemoteServiceDistributionProvider setDescription(String desc) {
		this.description = desc;
		return this;
	}

	protected boolean isServer() {
		return this.server;
	}

	protected RemoteServiceDistributionProvider setServer(boolean server) {
		this.server = server;
		return this;
	}

	protected boolean isHidden() {
		return this.hidden;
	}

	protected RemoteServiceDistributionProvider setHidden(boolean hidden) {
		this.hidden = hidden;
		return this;
	}

	public void validateComplete() throws NullPointerException {
		String ctdName = getName();
		if (ctdName == null)
			throw new NullPointerException("Container type description name cannot be null"); //$NON-NLS-1$
		IContainerInstantiator ctdInstantiator = getInstantiator();
		if (ctdInstantiator == null)
			throw new NullPointerException("Container type description instantiator cannot be null"); //$NON-NLS-1$
	}

	public ContainerTypeDescription createContainerTypeDescription() {
		validateComplete();
		return new ContainerTypeDescription(getName(), getInstantiator(), getDescription(), isServer(), isHidden());
	}

	public Dictionary<String, ?> getContainerTypeDescriptionProperties() {
		return null;
	}

	public Namespace createNamespace() {
		return null;
	}

	public Dictionary<String, ?> getNamespaceProperties() {
		return null;
	}

	public AdapterConfig createAdapterConfig() {
		return null;
	}
}
