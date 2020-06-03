/****************************************************************************
 * Copyright (c) 2009 EclipseSource and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.presence;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.IContainer;

/**
 * @since 2.0
 */
public class PresenceContainer implements IPresenceContainer {

	private IContainer container;
	private IPresenceContainerAdapter containerAdapter;

	public PresenceContainer(IContainer container, IPresenceContainerAdapter containerAdapter) {
		Assert.isNotNull(container);
		Assert.isNotNull(containerAdapter);
		this.container = container;
		this.containerAdapter = containerAdapter;
	}

	public IContainer getContainer() {
		return container;
	}

	public IPresenceContainerAdapter getContainerAdapter() {
		return containerAdapter;
	}

}
