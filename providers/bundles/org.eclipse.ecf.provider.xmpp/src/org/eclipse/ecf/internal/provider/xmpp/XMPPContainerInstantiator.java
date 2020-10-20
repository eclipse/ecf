/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.provider.xmpp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.provider.generic.GenericContainerInstantiator;
import org.eclipse.ecf.provider.xmpp.XMPPContainer;

public class XMPPContainerInstantiator extends GenericContainerInstantiator {
	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.ecf.core.provider.IContainerInstantiator#createInstance(
	 * ContainerDescription, java.lang.Object[])
	 */
	public IContainer createInstance(ContainerTypeDescription description,
			Object[] args) throws ContainerCreateException {
		try {
			Integer ka = Integer.valueOf(XMPPContainer.DEFAULT_KEEPALIVE);
			String name = null;
			if (args != null) {
				if (args.length > 0) {
					name = (String) args[0];
					if (args.length > 1) {
						ka = getIntegerFromArg(args[1]);
					}
				}
			}
			if (name == null) {
				if (ka == null) {
					return new XMPPContainer();
				} else {
					return new XMPPContainer(ka.intValue());
				}
			} else {
				if (ka == null) {
					ka = Integer.valueOf(XMPPContainer.DEFAULT_KEEPALIVE);
				}
				return new XMPPContainer(name, ka.intValue());
			}
		} catch (Exception e) {
			throw new ContainerCreateException(
					"Exception creating generic container", e);
		}
	}

	private static final String XMPP_CONFIG = "ecf.xmpp.smack"; //$NON-NLS-1$

	public String[] getImportedConfigs(ContainerTypeDescription description,
			String[] exporterSupportedConfigs) {
		if (exporterSupportedConfigs == null)
			return null;
		List results = new ArrayList();
		List supportedConfigs = Arrays.asList(exporterSupportedConfigs);
		if (XMPP_CONFIG.equals(description.getName())) {
			if (supportedConfigs.contains(XMPP_CONFIG))
				results.add(XMPP_CONFIG);
		}
		if (supportedConfigs.size() == 0)
			return null;
		return (String[]) results.toArray(new String[] {});
	}

	public String[] getSupportedConfigs(ContainerTypeDescription description) {
		return new String[] { XMPP_CONFIG };
	}

}
