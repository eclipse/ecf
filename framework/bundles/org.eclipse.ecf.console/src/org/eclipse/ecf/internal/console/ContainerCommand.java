/*******************************************************************************
 * Copyright (c) 2018 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.console;

import java.util.List;

import org.apache.felix.service.command.Converter;
import org.apache.felix.service.command.Descriptor;
import org.eclipse.ecf.console.AbstractCommand;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.core.identity.IIDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, property = { "osgi.command.scope=ecf", "osgi.command.function=listcontainers",
		"osgi.command.function=lcs", "osgi.command.function=listnamespaces", "osgi.command.function=lns",
		"osgi.command.function=listtypedescriptions",
		"osgi.command.function=lctds",
		"osgi.command.function=listconfigs",
		"osgi.command.function=lcfgs"}, service = { ContainerCommand.class, Converter.class })
public class ContainerCommand extends AbstractCommand implements Converter {

	@Reference
	private IContainerManager containerManager;
	@Reference
	private IIDFactory idFactory;
	
	protected IContainerManager getContainerManager() {
		return this.containerManager;
	}
	
	protected IIDFactory getIDFactory() {
		return this.idFactory;
	}
	
	@Descriptor("List ECF container instances")
	public List<IContainer> listcontainers() {
		return getContainers();
	}

	@Descriptor("List ECF container instances")
	public List<IContainer> lc() {
		return listcontainers();
	}

	public IContainer listcontainers(@Descriptor("Container ID to list (String)")IContainer container) {
		return container;
	}

	public IContainer lc(@Descriptor("Container ID to list (String)")IContainer container) {
		return container;
	}

	@Descriptor("List ECF ID namespaces")
	public List<Namespace> listnamespaces() {
		return getNamespaces();
	}

	@Descriptor("List ECF ID namespaces")
	public List<Namespace> lns() {
		return listnamespaces();
	}

	public Namespace listnamespaces(@Descriptor("Namespace name to list (String)") Namespace ns) {
		return ns;
	}

	public Namespace lns(@Descriptor("Namespace name to list (String)") Namespace ns) {
		return ns;
	}

	@Descriptor("List ECF container configs")
	public List<ContainerTypeDescription> listtypedescriptions() {
		return getConfigs();
	}

	@Descriptor("List ECF container configs")
	public List<ContainerTypeDescription> lctd() {
		return listtypedescriptions();
	}
	
	@Descriptor("List ECF container configs")
	public List<ContainerTypeDescription> listconfigs() {
		return getConfigs();
	}

	public List<ContainerTypeDescription> lcfgs() {
		return listconfigs();
	}
	
	public ContainerTypeDescription listtypedescriptions(@Descriptor("Config name to list (String)") ContainerTypeDescription ctd) {
		return ctd;
	}

	public ContainerTypeDescription lctds(@Descriptor("Config name to list (String)") ContainerTypeDescription ctd) {
		return ctd;
	}

	public ContainerTypeDescription listconfigs(@Descriptor("Config name to list (String)") ContainerTypeDescription ctd) {
		return ctd;
	}

	public ContainerTypeDescription lcfgs(@Descriptor("Config name to list (String)") ContainerTypeDescription ctd) {
		return ctd;
	}
	
}
