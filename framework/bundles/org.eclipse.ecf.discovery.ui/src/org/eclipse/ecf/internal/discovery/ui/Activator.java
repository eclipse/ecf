/****************************************************************************
 * Copyright (c) 2007 Remy Suen, Composent, Inc., and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.internal.discovery.ui;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ecf.discovery.ui"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private BundleContext context;

	private ServiceTracker extensionRegistryTracker = null;

	private ServiceTracker containerManagerTracker;

	/**
	 * The constructor
	 */
	public Activator() {
		// nothing
	}

	public IContainerManager getContainerManager() {
		if (containerManagerTracker == null) {
			containerManagerTracker = new ServiceTracker(context, IContainerManager.class.getName(), null);
			containerManagerTracker.open();
		}
		return (IContainerManager) containerManagerTracker.getService();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context1) throws Exception {
		super.start(context1);
		plugin = this;
		this.context = context1;
		ImageRegistry imageRegistry = getImageRegistry();
		imageRegistry.put("serviceIcon", imageDescriptorFromPlugin(PLUGIN_ID, "icons/default_provider_image.gif").createImage()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public Image getServiceImage() {
		return getImageRegistry().get("serviceIcon"); //$NON-NLS-1$
	}

	public IExtensionRegistry getExtensionRegistry() {
		if (extensionRegistryTracker == null) {
			this.extensionRegistryTracker = new ServiceTracker(context, IExtensionRegistry.class.getName(), null);
			this.extensionRegistryTracker.open();
		}
		return (IExtensionRegistry) extensionRegistryTracker.getService();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context1) throws Exception {
		super.stop(context1);
		if (extensionRegistryTracker != null) {
			extensionRegistryTracker.close();
			extensionRegistryTracker = null;
		}
		if (containerManagerTracker != null) {
			containerManagerTracker.close();
			containerManagerTracker = null;
		}
		plugin = null;
		this.context = null;
	}

	public static BundleContext getContext() {
		return getDefault().context;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
