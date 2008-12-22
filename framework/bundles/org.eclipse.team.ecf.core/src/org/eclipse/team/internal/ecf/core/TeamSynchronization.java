/******************************************************************************
 * Copyright (c) 2008 Versant Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Remy Chi Jian Suen (Versant Corporation) - initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ecf.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.datashare.IChannelContainerAdapter;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class TeamSynchronization extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ecf.sync.team"; //$NON-NLS-1$

	// The shared instance
	private static TeamSynchronization plugin;

	private static Map channels = new HashMap();

	public static void addShare(ID containerId, IChannelContainerAdapter channelContainer) throws ECFException {
		if (!channels.containsKey(containerId)) {
			channels.put(containerId, new RemoteShare(channelContainer));
		}
	}

	public static RemoteShare getShare(ID containerId) {
		return (RemoteShare) channels.get(containerId);
	}

	public static void removeShare(ID containerId) {
		channels.remove(containerId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static TeamSynchronization getDefault() {
		return plugin;
	}

	public static byte[] readFile(IFile file) {
		try {
			if (file.exists()) {
				InputStream contents = file.getContents();
				byte[] bytes = new byte[contents.available()];
				contents.read(bytes);
				return bytes;
			}
		} catch (IOException e) {
			TeamSynchronization.log("Could not read file content: " //$NON-NLS-1$
					+ file.getFullPath(), e);
		} catch (CoreException e) {
			TeamSynchronization.log("Could not retrieve file content: " //$NON-NLS-1$
					+ file.getFullPath(), e);
		}
		return null;
	}

	public static void log(String message) {
		log(message, null);
	}

	public static void log(String message, Throwable throwable) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, message, throwable));
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

}
