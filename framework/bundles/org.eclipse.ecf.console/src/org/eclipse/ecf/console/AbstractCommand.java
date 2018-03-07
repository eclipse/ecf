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

import org.apache.felix.service.command.CommandSession;
import org.apache.felix.service.command.Converter;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IIDFactory;
import org.eclipse.ecf.core.identity.Namespace;

public abstract class AbstractCommand {

	protected static final String CONTAINER_LINE_FORMAT = "%1$-45s|%2$-40s|%3$s"; //$NON-NLS-1$
	protected static final String CONTAINER_INSPECT_FORMAT = "ID=%s\n\tNamespace=%s\n\tClass=%s\n\tConnectedTo=%s\n\tConnectNamespace=%s\n\tConfig=%s"; //$NON-NLS-1$
	protected static final String NAMESPACE_LINE_FORMAT = "%s"; //$NON-NLS-1$
	protected static final String NAMESPACE_INSPECT_FORMAT = "ID=%s\n\tUriScheme=%s\n\tClass=%s\n\tDescription=%s\n\tInstanceArgTypes=%s"; //$NON-NLS-1$
	protected static final String CTD_LINE_FORMAT = "%s"; //$NON-NLS-1$
	protected static final String CTD_INSPECT_FORMAT = "ID=%s\n\tDescription=%s\n\tSupportedConfigs=%s\n\tSupportedIntents=%s\n\tInstanceArgTypes=%s\n\tAdapters=%s\n\tHidden=%b\n\tServer=%b"; //$NON-NLS-1$

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

	protected void consoleLine(CommandSession cs, String format, Object... args) {
		cs.getConsole().format(format, args);
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
			return ""; //$NON-NLS-1$
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < types.length; i++) {
			Class<?>[] paramTypes = types[i];
			sb.append("("); //$NON-NLS-1$
			for (int j = 0; j < paramTypes.length; j++) {
				Class<?> pType = paramTypes[j];
				sb.append(pType.getName());
				if (j + 1 < paramTypes.length)
					sb.append(","); //$NON-NLS-1$
			}
  		    sb.append(")"); //$NON-NLS-1$
  		    if (i + 1 < types.length)
  		    	sb.append(";");
		}
		return sb.toString();
	}

	protected String formatLine(String format, Object... args) {
		try (Formatter f = new Formatter();) {
			f.format(format, args); // $NON-NLS-1$
			return f.toString();
		}
	}

	protected String formatContainer(IContainer c, int level, Converter escape) {
		ID cID = c.getID();
		ID conID = c.getConnectedID();
		String conIDStr = (conID == null) ? "" : conID.getName(); //$NON-NLS-1$
		Class<?> cClass = c.getClass();
		switch (level) {
		case Converter.LINE:
			return formatLine(CONTAINER_LINE_FORMAT, cID.getName(), cClass.getSimpleName(), conIDStr);
		case Converter.INSPECT:
			return formatLine(CONTAINER_INSPECT_FORMAT, cID.getName(), cID.getNamespace().getName(), cClass.getName(),
					conIDStr, c.getConnectNamespace().getName(), getContainerTypeDescription(cID).getName());
		default:
			return null;
		}
	}

	protected String formatNamespace(Namespace ns, int level, Converter escape) {
		switch (level) {
		case Converter.PART:
			return null;
		case Converter.LINE:
			return formatLine(NAMESPACE_LINE_FORMAT, ns.getName());
		case Converter.INSPECT:
			return formatLine(NAMESPACE_INSPECT_FORMAT, ns.getName(), ns.getScheme(), ns.getClass().getName(),
					ns.getDescription(), printClassArrays(ns.getSupportedParameterTypes()));
		default:
			return null;
		}
	}

	protected String formatConfig(ContainerTypeDescription ctd, int level, Converter escape) {
		switch (level) {
		case Converter.PART:
			return null;
		case Converter.LINE:
			return formatLine(CTD_LINE_FORMAT, ctd.getName());
		case Converter.INSPECT:
			return formatLine(CTD_INSPECT_FORMAT, ctd.getName(), ctd.getDescription(),
					printStringArray(ctd.getSupportedConfigs()), printStringArray(ctd.getSupportedIntents()),
					printClassArrays(ctd.getSupportedParameterTypes()),
					printStringArray(ctd.getSupportedAdapterTypes()), ctd.isHidden(), ctd.isServer());
		default:
			return null;
		}
	}

	protected String printStringArray(String[] strarr) {
		return (strarr == null) ? "" : Arrays.asList(strarr).toString(); //$NON-NLS-1$
	}

}
