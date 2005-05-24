/*******************************************************************************
 * Copyright (c) 2005 Peter Nehrer and Composent, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Peter Nehrer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.internal.datashare;

import java.io.PrintStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.datashare.DataShareServiceFactory;
import org.eclipse.ecf.datashare.IDataShareServiceManager;
import org.eclipse.ecf.datashare.IUpdateProviderFactory;
import org.eclipse.ecf.datashare.UpdateProviderRegistry;
import org.osgi.framework.BundleContext;

/**
 * @author pnehrer
 */
public class DataSharePlugin {

	public static final String PLUGIN_ID = "org.eclipse.ecf.datashare";

	private static final String TRACE_PREFIX = PLUGIN_ID + "/";

	private static EclipsePlugin plugin;

	private static boolean tracingEnabled = Boolean.getBoolean(TRACE_PREFIX
			+ "debug");

	private DataSharePlugin() {
	}

	public static void log(Object entry) {
		if (plugin == null) {
			if (entry instanceof Throwable)
				((Throwable) entry).printStackTrace();
			else
				System.err.println(entry);
		} else {
			plugin.log(entry);
		}
	}

	public static boolean isTracing(String tag) {
		if (tracingEnabled) {
			return plugin == null ? Boolean.getBoolean(TRACE_PREFIX + tag)
					: plugin.isTracing(tag);
		} else
			return false;
	}

	public static PrintStream getTraceLog() {
		return System.out;
	}

	public static class EclipsePlugin extends Plugin {

		private static final String MANAGER_EXTENSION_POINT = "servicemanager";

		private static final String MANAGER_EXTENSION = "manager";

		private static final String ATTR_NAME = "name";

		private static final String ATTR_CLASS = "class";

		private static final String PROVIDER_EXTENSION_POINT = "updateprovider";

		private static final String PROVIDER_EXTENSION = "updateProvider";

		private static final String ATTR_ID = "id";

		private static final String ATTR_FACTORY = "factory";

		private IRegistryChangeListener registryChangeListener;

		public EclipsePlugin() {
			plugin = this;
			tracingEnabled = Platform.inDebugMode();
		}

		/**
		 * This method is called upon plug-in activation
		 */
		public void start(BundleContext context) throws Exception {
			super.start(context);
			final String namespace = getBundle().getSymbolicName();
			registryChangeListener = new IRegistryChangeListener() {
				public void registryChanged(IRegistryChangeEvent event) {
					IExtensionDelta[] deltas = event
							.getExtensionDeltas(namespace);
					for (int i = 0; i < deltas.length; ++i) {
						IConfigurationElement[] elems = deltas[i]
								.getExtension().getConfigurationElements();
						switch (deltas[i].getKind()) {
						case IExtensionDelta.ADDED:
							if (deltas[i].getExtensionPoint()
									.getSimpleIdentifier().equals(
											MANAGER_EXTENSION_POINT))
								registerManagers(elems);
							else if (deltas[i].getExtensionPoint()
									.getSimpleIdentifier().equals(
											PROVIDER_EXTENSION_POINT))
								registerProviders(elems);

							break;

						case IExtensionDelta.REMOVED:
							if (deltas[i].getExtensionPoint()
									.getSimpleIdentifier().equals(
											MANAGER_EXTENSION_POINT))
								unregisterManagers(elems);
							else if (deltas[i].getExtensionPoint()
									.getSimpleIdentifier().equals(
											PROVIDER_EXTENSION_POINT))
								unregisterProviders(elems);
							break;
						}
					}
				}
			};

			IExtensionRegistry reg = Platform.getExtensionRegistry();
			reg.addRegistryChangeListener(registryChangeListener, namespace);
			IConfigurationElement[] elems = reg.getConfigurationElementsFor(
					namespace, MANAGER_EXTENSION_POINT);
			registerManagers(elems);
			elems = reg.getConfigurationElementsFor(namespace,
					PROVIDER_EXTENSION_POINT);
			registerProviders(elems);
		}

		private void registerManagers(IConfigurationElement[] elems) {
			for (int i = 0; i < elems.length; ++i) {
				if (!MANAGER_EXTENSION.equals(elems[i].getName()))
					continue;

				String name = elems[i].getAttribute(ATTR_NAME);
				if (name == null || name.length() == 0)
					continue;

				IDataShareServiceManager mgr;
				try {
					mgr = (IDataShareServiceManager) elems[i]
							.createExecutableExtension(ATTR_CLASS);
				} catch (Exception ex) {
					continue;
				}

				DataShareServiceFactory.registerManager(name, mgr);
			}
		}

		private void registerProviders(IConfigurationElement[] elems) {
			for (int i = 0; i < elems.length; ++i) {
				if (!PROVIDER_EXTENSION.equals(elems[i].getName()))
					continue;

				String id = elems[i].getAttribute(ATTR_ID);
				if (id == null || id.length() == 0)
					continue;

				IUpdateProviderFactory factory;
				try {
					factory = (IUpdateProviderFactory) elems[i]
							.createExecutableExtension(ATTR_FACTORY);
				} catch (Exception ex) {
					continue;
				}

				UpdateProviderRegistry.registerFactory(id, factory);
			}
		}

		private void unregisterManagers(IConfigurationElement[] elems) {
			for (int i = 0; i < elems.length; ++i) {
				if (!MANAGER_EXTENSION.equals(elems[i].getName()))
					continue;

				String name = elems[i].getAttribute(ATTR_NAME);
				if (name != null && name.length() > 0)
					DataShareServiceFactory.unregisterManager(name);
			}
		}

		private void unregisterProviders(IConfigurationElement[] elems) {
			for (int i = 0; i < elems.length; ++i) {
				if (!PROVIDER_EXTENSION.equals(elems[i].getName()))
					continue;

				String id = elems[i].getAttribute(ATTR_ID);
				if (id != null && id.length() > 0)
					UpdateProviderRegistry.unregisterFactory(id);
			}
		}

		/**
		 * This method is called when the plug-in is stopped
		 */
		public void stop(BundleContext context) throws Exception {
			if (registryChangeListener != null)
				Platform.getExtensionRegistry().removeRegistryChangeListener(
						registryChangeListener);

			DataShareServiceFactory.unregisterAllManagers();
			UpdateProviderRegistry.unregisterAllFactories();
			plugin = null;
			super.stop(context);
		}

		public void log(Object entry) {
			IStatus status;
			if (entry instanceof IStatus)
				status = (IStatus) entry;
			else if (entry instanceof CoreException)
				status = ((CoreException) entry).getStatus();
			else if (entry instanceof Throwable) {
				Throwable t = (Throwable) entry;
				status = new Status(Status.ERROR,
						getBundle().getSymbolicName(), 0,
						t.getLocalizedMessage() == null ? "Unknown error." : t
								.getLocalizedMessage(), t);
			} else
				status = new Status(Status.WARNING, getBundle()
						.getSymbolicName(), 0, String.valueOf(entry),
						new RuntimeException().fillInStackTrace());

			getLog().log(status);
		}

		public boolean isTracing(String tag) {
			return Boolean.TRUE.equals(Boolean.valueOf(Platform
					.getDebugOption(TRACE_PREFIX + tag)));
		}
	}
}
