/****************************************************************************
* Copyright (c) 2004 Composent, Inc. and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Composent, Inc. - initial API and implementation
*****************************************************************************/

package org.eclipse.ecf.example.collab;

import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class ClientPlugin extends AbstractUIPlugin implements ClientPluginConstants {

    public static final String PLUGIN_ID = "org.eclipse.ecf.example.collab";
    
	//The shared instance.
	private static ClientPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	
	private static String appShareBinPath;
	private static URL pluginLocation;
	
	private ImageRegistry registry = null;
	
	public static String getAppShareBinPath() {
		return appShareBinPath;
	}
	public static URL getPluginTopLocation() {
		return pluginLocation;
	}
	public static void log(String message) {
		getDefault().getLog().log(new Status(IStatus.OK, PLUGIN_ID, IStatus.OK, message, null));
	}

	public static void log(String message, Throwable e) {
		getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, "Caught exception", e));
	}

	/**
	 * The constructor.
	 */
	public ClientPlugin() {
		super();
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
		resourceBundle = null;
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#createImageRegistry()
	 */
	protected ImageRegistry createImageRegistry() {
		registry = super.createImageRegistry();
		
  
		registry.put(ClientPluginConstants.DECORATION_PROJECT, PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT));
		registry.put(ClientPluginConstants.DECORATION_USER, PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE));
		registry.put(ClientPluginConstants.DECORATION_TIME, PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_FORWARD));
		registry.put(ClientPluginConstants.DECORATION_TASK, PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER));
		
		registry.put(ClientPluginConstants.DECORATION_SEND , PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_UNDO));
		registry.put(ClientPluginConstants.DECORATION_RECEIVE , PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_REDO));
		registry.put(ClientPluginConstants.DECORATION_PRIVATE , PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK));
		registry.put(ClientPluginConstants.DECORATION_SYSTEM_MESSAGE , PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK));
		return registry;
	}
	/**
	 * Returns the shared instance.
	 */
	public static ClientPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = ClientPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		try {
			if (resourceBundle == null)
				resourceBundle = ResourceBundle.getBundle("org.eclipse.ecf.example.collab.ClientPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		return resourceBundle;
	}

}
