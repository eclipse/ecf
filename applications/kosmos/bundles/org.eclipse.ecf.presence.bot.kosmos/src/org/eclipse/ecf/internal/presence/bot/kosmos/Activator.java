/****************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.presence.bot.kosmos;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static Activator instance;

	private Bundle bundle;

	public Activator() {
		instance = this;
	}

	public void start(BundleContext context) throws Exception {
		bundle = context.getBundle();
	}

	public void stop(BundleContext context) throws Exception {
		bundle = null;
		instance = null;
	}

	static Bundle getBundle() {
		return instance.bundle;
	}

}
