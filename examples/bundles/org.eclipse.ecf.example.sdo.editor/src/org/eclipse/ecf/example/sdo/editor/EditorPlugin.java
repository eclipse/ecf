/*******************************************************************************
 * Copyright (c) 2004 Peter Nehrer and Composent, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Peter Nehrer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.example.sdo.editor;

import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.ISharedObjectContainer;
import org.eclipse.ecf.core.SharedObjectContainerFactory;
import org.eclipse.ecf.core.SharedObjectContainerInstantiationException;
import org.eclipse.ecf.core.SharedObjectContainerJoinException;
import org.eclipse.ecf.core.SharedObjectCreateException;
import org.eclipse.ecf.core.SharedObjectDescription;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.IDInstantiationException;
import org.eclipse.ecf.sdo.ISharedDataGraph;
import org.eclipse.ecf.sdo.ISubscriptionCallback;
import org.eclipse.ecf.sdo.IUpdateConsumer;
import org.eclipse.ecf.sdo.SDOPlugin;
import org.eclipse.ecf.sdo.emf.EMFUpdateProvider;
import org.eclipse.ecf.test.EventSpy;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import commonj.sdo.DataGraph;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @author pnehrer
 */
public class EditorPlugin extends AbstractUIPlugin {
	// The shared instance.
	private static EditorPlugin plugin;

	// Resource bundle.
	private ResourceBundle resourceBundle;

	private static final String CONTAINER_ID = "org.eclipse.ecf.test.TestContainer";

	private static final String GROUP_ID = "test.sdo";

	private final Hashtable published = new Hashtable();

	/**
	 * The constructor.
	 */
	public EditorPlugin() {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle
					.getBundle("org.eclipse.ecf.example.sdo.editor.EditorPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
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
	}

	/**
	 * Returns the shared instance.
	 */
	public static EditorPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not
	 * found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = EditorPlugin.getDefault().getResourceBundle();
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
		return resourceBundle;
	}

	public void log(Throwable t) {
		if (t instanceof CoreException)
			getLog().log(((CoreException) t).getStatus());
		else
			getLog().log(
					new Status(Status.ERROR, getBundle().getSymbolicName(), 0,
							"An unexpected error occurred.", t));
	}

	private ISharedObjectContainer createContainer(String spyName)
			throws CoreException {
		ISharedObjectContainer container;
		try {
			container = SharedObjectContainerFactory
					.makeSharedObjectContainer(CONTAINER_ID);
		} catch (SharedObjectContainerInstantiationException e) {
			throw new CoreException(new Status(Status.ERROR, getBundle()
					.getSymbolicName(), 0,
					"Could not create container with ID " + CONTAINER_ID + ".",
					e));
		}

		try {
			container.getSharedObjectManager().createSharedObject(
					new SharedObjectDescription(
							IDFactory.makeStringID(spyName), EventSpy.class),
					null);
		} catch (SharedObjectCreateException e) {
			log(e);
		} catch (IDInstantiationException e) {
			log(e);
		}

		try {
			container.joinGroup(IDFactory.makeStringID(GROUP_ID), null);
		} catch (IDInstantiationException e) {
			throw new CoreException(new Status(Status.ERROR, getBundle()
					.getSymbolicName(), 0, "Could not join group with ID "
					+ GROUP_ID + ".", e));
		} catch (SharedObjectContainerJoinException e) {
			throw new CoreException(new Status(Status.ERROR, getBundle()
					.getSymbolicName(), 0, "Could not join group with ID "
					+ GROUP_ID + ".", e));
		}

		return container;
	}

	public ISharedDataGraph subscribe(String path,
			ISubscriptionCallback callback, IUpdateConsumer consumer)
			throws CoreException {

		ISharedObjectContainer container = createContainer("subscribe");
		try {
			return SDOPlugin.getDefault().getDataGraphSharing(container)
					.subscribe(IDFactory.makeStringID(path), callback,
							new EMFUpdateProvider(), consumer);
		} catch (IDInstantiationException e) {
			throw new CoreException(new Status(Status.ERROR, getBundle()
					.getSymbolicName(), 0,
					"Could not subscribe to graph with id " + path + ".", e));
		}
	}

	public ISharedDataGraph publish(String path, DataGraph dataGraph,
			IUpdateConsumer consumer) throws CoreException {
		SDOPlugin.getDefault().setDebug(true);
		ISharedObjectContainer container = createContainer("publish");
		try {
			ISharedDataGraph sdg = SDOPlugin.getDefault().getDataGraphSharing(
					container).publish(dataGraph, IDFactory.makeStringID(path),
					new EMFUpdateProvider(), consumer);
			published.put(path, container);
			return sdg;
		} catch (IDInstantiationException e) {
			throw new CoreException(new Status(Status.ERROR, getBundle()
					.getSymbolicName(), 0, "Could not publish graph with id "
					+ path + ".", e));
		}
	}

	public void dispose(String path) {
		ISharedObjectContainer container = (ISharedObjectContainer) published
				.get(path);
		if (container != null)
			container.dispose(0);
	}

	public boolean isPublished(String path) {
		return published.contains(path);
	}
}