/****************************************************************************
 * Copyright (c) 2018 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.console;

import java.util.List;

import org.apache.felix.service.command.CommandSession;
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
		"osgi.command.function=listtypedescriptions", "osgi.command.function=lctds",
		"osgi.command.function=listconfigs",
		"osgi.command.function=lcfgs" }, service = { ContainerCommand.class, Converter.class })
public class ContainerCommand extends AbstractCommand implements Converter {

	private IContainerManager containerManager;
	private IIDFactory idFactory;

	@Reference
	void bindContainerManager(IContainerManager cm) {
		this.containerManager = cm;
	}

	void unbindContainerManager(IContainerManager cm) {
		this.containerManager = null;
	}

	@Reference
	void bindIdentityFactory(IIDFactory idf) {
		this.idFactory = idf;
	}

	void unbindIdentityFactory(IIDFactory idf) {
		this.idFactory = null;
	}

	protected IContainerManager getContainerManager() {
		return this.containerManager;
	}

	protected IIDFactory getIDFactory() {
		return this.idFactory;
	}

	@Descriptor("List ECF container instances")
	public List<IContainer> listcontainers(CommandSession cs) {
		consoleLine(cs, CONTAINER_LINE_FORMAT, "ID", "ImplClass", "Connected\n");
		return getContainers();
	}

	@Descriptor("List ECF container instances")
	public List<IContainer> lcs(CommandSession cs) {
		return listcontainers(cs);
	}

	public IContainer listcontainers(@Descriptor("Container ID to list (String)") IContainer container) {
		return container;
	}

	public IContainer lcs(@Descriptor("Container ID to list (String)") IContainer container) {
		return container;
	}

	@Descriptor("List ECF ID namespaces")
	public List<Namespace> listnamespaces(CommandSession cs) {
		consoleLine(cs, NAMESPACE_LINE_FORMAT, "Namespace Name\n");
		return getNamespaces();
	}

	@Descriptor("List ECF ID namespaces")
	public List<Namespace> lns(CommandSession cs) {
		return listnamespaces(cs);
	}

	public Namespace listnamespaces(@Descriptor("Namespace name to list (String)") Namespace ns) {
		return ns;
	}

	public Namespace lns(@Descriptor("Namespace name to list (String)") Namespace ns) {
		return ns;
	}

	@Descriptor("List ECF container configs")
	public List<ContainerTypeDescription> listtypedescriptions(CommandSession cs) {
		consoleLine(cs, CTD_LINE_FORMAT, "ContainerTypeDescription Name\n");
		return getConfigs();
	}

	@Descriptor("List ECF container configs")
	public List<ContainerTypeDescription> lctds(CommandSession cs) {
		return listtypedescriptions(cs);
	}

	@Descriptor("List ECF container configs")
	public List<ContainerTypeDescription> listconfigs(CommandSession cs) {
		cs.getConsole().format(CTD_LINE_FORMAT, "Config Name\n");
		return getConfigs();
	}

	public List<ContainerTypeDescription> lcfgs(CommandSession cs) {
		return listconfigs(cs);
	}

	public ContainerTypeDescription listtypedescriptions(
			@Descriptor("Container type description name to list (String)") ContainerTypeDescription ctd) {
		return ctd;
	}

	public ContainerTypeDescription lctds(
			@Descriptor("Container type description name to list (String)") ContainerTypeDescription ctd) {
		return ctd;
	}

	public ContainerTypeDescription listconfigs(
			@Descriptor("Config name to list (String)") ContainerTypeDescription ctd) {
		return ctd;
	}

	public ContainerTypeDescription lcfgs(@Descriptor("Config name to list (String)") ContainerTypeDescription ctd) {
		return ctd;
	}

	public Object convert(Class<?> desiredType, Object in) throws Exception {
		if (desiredType == IContainer.class && in instanceof String)
			return getContainerForId((String) in);
		else if (desiredType == Namespace.class && in instanceof String)
			return getIDFactory().getNamespaceByName((String) in);
		else if (desiredType == ContainerTypeDescription.class)
			return getContainerManager().getContainerFactory().getDescriptionByName((String) in);
		return null;
	}

	public String format(Object target, int level, Converter escape) {
		if (target instanceof IContainer)
			return formatContainer((IContainer) target, level, escape);
		else if (target instanceof Namespace)
			return formatNamespace((Namespace) target, level, escape);
		else if (target instanceof ContainerTypeDescription)
			return formatConfig((ContainerTypeDescription) target, level, escape);
		return null;
	}

}
