/*******************************************************************************
 * Copyright (c) 2018 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;

import org.apache.felix.service.command.Converter;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IIDFactory;
import org.eclipse.ecf.core.identity.Namespace;

public abstract class AbstractCommand {

	protected abstract IContainerManager getContainerManager();

	protected abstract IIDFactory getIDFactory();

	protected List<IContainer> getContainers() {
		return Arrays.asList(getContainerManager().getAllContainers().clone());
	}

	protected List<Namespace> getNamespaces() {
		return new ArrayList<Namespace>(getIDFactory().getNamespaces());
	}

	@SuppressWarnings("unchecked")
	protected List<ContainerTypeDescription> getConfigs() {
		return new ArrayList<ContainerTypeDescription>(getContainerManager().getContainerFactory().getDescriptions());

	}

	protected ContainerTypeDescription getContainerTypeDescription(ID containerID) {
		return getContainerManager().getContainerTypeDescription(containerID);
	}

	protected IContainer getContainerForId(String id) {
		for (IContainer c : getContainerManager().getAllContainers().clone())
			if (c.getID().getName().equals(id))
				return c;
		return null;
	}

	protected String printClassArrays(Class<?>[][] types) {
		if (types == null)
			return "";
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < types.length; i++) {
			Class<?>[] paramTypes = types[i];
			sb.append("<init>").append("(");
			for (int j = 0; j < paramTypes.length; j++) {
				Class<?> pType = paramTypes[j];
				sb.append(pType.getName());
				if (j + 1 < paramTypes.length)
					sb.append(",");
			}
			sb.append(")");
			if (i + 1 < types.length)
				sb.append(";");
		}
		return sb.toString();
	}

	protected String formatContainer(IContainer c, int level, Converter escape) {
		ID cID = c.getID();
		ID conID = c.getConnectedID();
		String conIDStr = (conID == null) ? "" : conID.getName();
		Class<?> cClass = c.getClass();
		switch (level) {
		case Converter.LINE:
			try (Formatter f = new Formatter();) {
				f.format("%s        %s        %s", cID.getName(), cClass.getName(), conIDStr);
				return f.toString();
			}
		case Converter.INSPECT:
			try (Formatter f = new Formatter();) {
				f.format(
						"ID%s\n" + "\tIDNamespace=%s\n" + "\tImplClass=%s\n" + "\tConnectedTo=%s\n"
								+ "\tConnectNamespace=%s\n" + "\tConfig=%s",
						cID.getName(), cID.getNamespace().getName(), cClass.getName(), conIDStr,
						c.getConnectNamespace().getName(), getContainerTypeDescription(cID).getName());
				return f.toString();
			}
		default:
			return null;
		}
	}

	protected String formatNamespace(Namespace ns, int level, Converter escape) {
		switch (level) {
		case Converter.PART:
			return null;
		case Converter.LINE:
			try (Formatter f = new Formatter();) {
				f.format("%s", ns.getName());
				return f.toString();
			}
		case Converter.INSPECT:
			try (Formatter f = new Formatter();) {
				f.format(
						"ID=%s\n" + "\tScheme=%s\n" + "\tImplClass=%s\n" + "\tDescription=%s\n"
								+ "\tFactoryConstructors=%s\n" + "\tSupportedSchemes=%s",
						ns.getName(), ns.getScheme(), ns.getClass().getName(), ns.getDescription(),
						printClassArrays(ns.getSupportedParameterTypes()),
						printStringArray(ns.getSupportedSchemes()));
				return f.toString();
			}
		default:
			return null;
		}
	}

	protected String formatConfig(ContainerTypeDescription ctd, int level, Converter escape) {
		switch (level) {
		case Converter.PART:
			return null;
		case Converter.LINE:
			try (Formatter f = new Formatter();) {
				f.format("%s", ctd.getName());
				return f.toString();
			}
		case Converter.INSPECT:
			try (Formatter f = new Formatter();) {
				f.format(
						"ID=%s\n" + "\tDescription=%s\n" + "\tSupportedConfigs=%s\n" + "\tSupportedIntents=%s\n"
								+ "\tFactoryConstructors=%s\n" + "\tAdapterTypes=%s\n" + "\tHidden=%b\n" + "\tServer=%b",
						ctd.getName(), ctd.getDescription(), printStringArray(ctd.getSupportedConfigs()),
						printStringArray(ctd.getSupportedIntents()),
						printClassArrays(ctd.getSupportedParameterTypes()),
						printStringArray(ctd.getSupportedAdapterTypes()),
						ctd.isHidden(), ctd.isServer());
				return f.toString();
			}
		default:
			return null;
		}
	}

	protected String printStringArray(String[] strarr) {
		return (strarr == null) ? "" : Arrays.asList(strarr).toString();
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
