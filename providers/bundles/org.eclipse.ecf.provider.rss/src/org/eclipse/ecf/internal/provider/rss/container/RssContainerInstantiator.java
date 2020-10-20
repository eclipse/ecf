/****************************************************************************
 * Copyright (c) 2006 Parity Communications, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Sergey Yakovlev - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.provider.rss.container;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.provider.generic.GenericContainerInstantiator;

/**
 * 
 */
public class RssContainerInstantiator extends GenericContainerInstantiator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#createInstance(org.eclipse.ecf.core.ContainerDescription,
	 *      java.lang.Class[], java.lang.Object[])
	 */
	public IContainer createInstance(ContainerTypeDescription description,
			Object[] args) throws ContainerCreateException {
		try {
			Integer keepAlive = Integer.valueOf(
					RssClientSOContainer.DEFAULT_KEEPALIVE);
			String name = null;

			if (args != null) {
				if (args.length > 0) {
					name = (String) args[0];
					if (args.length > 1) {
						keepAlive = getIntegerFromArg(args[1]);
					}
				}
			}

			if (name == null) {
				if (keepAlive == null) {
					return new RssClientSOContainer();
				} else {
					return new RssClientSOContainer(keepAlive.intValue());
				}
			} else {
				if (keepAlive == null) {
					keepAlive = Integer.valueOf(
							RssClientSOContainer.DEFAULT_KEEPALIVE);
				}
				return new RssClientSOContainer(name, keepAlive.intValue());
			}
		} catch (Exception e) {
			throw new ContainerCreateException(
					"Exception creating RSS container", e);
		}
	}

	protected Integer getIntegerFromArg(Object arg)
			throws NumberFormatException {
		if (arg instanceof Integer) {
			return (Integer) arg;
		} else if (arg != null) {
			return Integer.valueOf((String) arg);
		} else {
			return Integer.valueOf(-1);
		}
	}

}
