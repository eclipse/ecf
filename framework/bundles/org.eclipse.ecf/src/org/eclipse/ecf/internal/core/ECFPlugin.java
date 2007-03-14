/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.core;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainerFactory;
import org.eclipse.ecf.core.provider.IContainerInstantiator;
import org.eclipse.ecf.core.start.ECFStartJob;
import org.eclipse.ecf.core.start.IECFStart;
import org.eclipse.ecf.core.util.LogHelper;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

public class ECFPlugin implements BundleActivator {

	public static final String PLUGIN_ID = "org.eclipse.ecf"; //$NON-NLS-1$

	private static final String ECFNAMESPACE = PLUGIN_ID;

	private static final String CONTAINER_FACTORY_NAME = "containerFactory"; //$NON-NLS-1$

	private static final String CONTAINER_FACTORY_EPOINT = ECFNAMESPACE
			+ "." + CONTAINER_FACTORY_NAME; //$NON-NLS-1$

	private static final String STARTUP_NAME = "startup"; //$NON-NLS-1$

	public static final String START_EPOINT = ECFNAMESPACE + "." + STARTUP_NAME; //$NON-NLS-1$

	public static final String PLUGIN_RESOURCE_BUNDLE = ECFNAMESPACE
			+ ".ECFPluginResources"; //$NON-NLS-1$

	public static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$

	public static final String NAME_ATTRIBUTE = "name"; //$NON-NLS-1$

	public static final String DESCRIPTION_ATTRIBUTE = "description"; //$NON-NLS-1$

	public static final String ARG_ELEMENT_NAME = "defaultargument"; //$NON-NLS-1$

	public static final String VALUE_ATTRIBUTE = "value"; //$NON-NLS-1$

	public static final String PROPERTY_ELEMENT_NAME = "property"; //$NON-NLS-1$

	public static final int FACTORY_DOES_NOT_IMPLEMENT_ERRORCODE = 10;

	public static final int FACTORY_NAME_COLLISION_ERRORCODE = 20;

	public static final int INSTANTIATOR_DOES_NOT_IMPLEMENT_ERRORCODE = 30;

	public static final int INSTANTIATOR_NAME_COLLISION_ERRORCODE = 50;

	public static final int INSTANTIATOR_NAMESPACE_LOAD_ERRORCODE = 60;

	public static final int START_ERRORCODE = 70;

	protected static final int REMOVE_NAMESPACE_ERRORCODE = 80;

	// The shared instance.
	private static ECFPlugin plugin;
	
	private BundleContext context = null;

	private ServiceTracker extensionRegistryTracker = null;

	private Map disposables = new WeakHashMap();

	private IRegistryChangeListener registryManager = null;

	private ServiceRegistration containerFactoryServiceRegistration;

	private ServiceTracker logServiceTracker = null;

	public ECFPlugin() {
	}

	public void addDisposable(IDisposable disposable) {
		disposables.put(disposable, null);
	}

	public void removeDisposable(IDisposable disposable) {
		disposables.remove(disposable);
	}

	protected void fireDisposables() {
		for (Iterator i = disposables.keySet().iterator(); i.hasNext();) {
			IDisposable d = (IDisposable) i.next();
			if (d != null)
				d.dispose();
		}
	}

	public Bundle getBundle() {
		if (context == null) return null;
		return context.getBundle();
	}
	
	protected LogService getLogService() {
		if (logServiceTracker == null) {
			logServiceTracker = new ServiceTracker(this.context,
					LogService.class.getName(), null);
			logServiceTracker.open();
		}
		return (LogService) logServiceTracker.getService();
	}

	public void log(IStatus status) {
		LogService logService = getLogService();
		if (logService != null) {
			logService.log(LogHelper.getLogCode(status), LogHelper
					.getLogMessage(status), status.getException());
		}
	}

	public IAdapterManager getAdapterManager() {
		// XXX todo...replace with new adaptermanager service
		return Platform.getAdapterManager();
		//return null;
	}


	protected String[] getDefaultArgs(IConfigurationElement[] argElements) {
		String[] argDefaults = new String[0];
		if (argElements != null) {
			if (argElements.length > 0) {
				argDefaults = new String[argElements.length];
				for (int i = 0; i < argElements.length; i++)
					argDefaults[i] = argElements[i]
							.getAttribute(VALUE_ATTRIBUTE);
			}
		}
		return argDefaults;
	}

	protected Map getProperties(IConfigurationElement[] propertyElements) {
		Properties props = new Properties();
		if (propertyElements != null) {
			if (propertyElements.length > 0) {
				for (int i = 0; i < propertyElements.length; i++) {
					String name = propertyElements[i]
							.getAttribute(NAME_ATTRIBUTE);
					String value = propertyElements[i]
							.getAttribute(VALUE_ATTRIBUTE);
					if (name != null && !name.equals("") && value != null //$NON-NLS-1$
							&& !value.equals("")) { //$NON-NLS-1$
						props.setProperty(name, value);
					}
				}
			}
		}
		return props;
	}

	protected void logException(IStatus status, String method,
			Throwable exception) {
		log(status);
		Trace.catching(ECFPlugin.PLUGIN_ID,
				ECFDebugOptions.EXCEPTIONS_CATCHING, ECFPlugin.class, method,
				exception);
	}

	/**
	 * Remove extensions for container factory extension point
	 * 
	 * @param members
	 *            the members to remove
	 */
	protected void removeContainerFactoryExtensions(
			IConfigurationElement[] members) {
		String method = "removeContainerFactoryExtensions"; //$NON-NLS-1$
		Trace.entering(ECFPlugin.PLUGIN_ID,
				ECFDebugOptions.METHODS_ENTERING, ECFPlugin.class, method,
				members);
		// For each configuration element
		for (int m = 0; m < members.length; m++) {
			IConfigurationElement member = members[m];
			// Get the label of the extender plugin and the ID of the extension.
			IExtension extension = member.getDeclaringExtension();
			String name = null;
			try {
				// Get name and get version, if available
				name = member.getAttribute(NAME_ATTRIBUTE);
				if (name == null) {
					name = member.getAttribute(CLASS_ATTRIBUTE);
				}
				IContainerFactory factory = ContainerFactory.getDefault();
				ContainerTypeDescription cd = factory
						.getDescriptionByName(name);
				if (cd == null || !factory.containsDescription(cd)) {
					continue;
				}
				// remove
				factory.removeDescription(cd);
				Trace.trace(ECFPlugin.PLUGIN_ID, ECFDebugOptions.DEBUG,
						method + ".removed " + cd + " from factory"); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (Exception e) {
				logException(
						new Status(
								Status.ERROR,
								getDefault().getBundle().getSymbolicName(),
								FACTORY_NAME_COLLISION_ERRORCODE,
								NLS
										.bind(
												Messages.ECFPlugin_Container_Name_Collision_Prefix,
												name,
												extension
														.getExtensionPointUniqueIdentifier()),
								null), method, e);
			}
		}
	}

	/**
	 * Add container factory extension point extensions
	 * 
	 * @param members
	 *            to add
	 */
	protected void addContainerFactoryExtensions(IConfigurationElement[] members) {
		String method = "addContainerFactoryExtensions"; //$NON-NLS-1$
		Trace.entering(ECFPlugin.PLUGIN_ID,
				ECFDebugOptions.METHODS_ENTERING, ECFPlugin.class, method,
				members);
		// For each configuration element
		for (int m = 0; m < members.length; m++) {
			IConfigurationElement member = members[m];
			// Get the label of the extender plugin and the ID of the extension.
			IExtension extension = member.getDeclaringExtension();
			Object exten = null;
			String name = null;
			try {
				// The only required attribute is "class"
				exten = member.createExecutableExtension(CLASS_ATTRIBUTE);
				String clazz = exten.getClass().getName();
				// Get name and get version, if available
				name = member.getAttribute(NAME_ATTRIBUTE);
				if (name == null) {
					name = clazz;
				}
				// Get description, if present
				String description = member.getAttribute(DESCRIPTION_ATTRIBUTE);
				if (description == null) {
					description = ""; //$NON-NLS-1$
				}

				// Get any arguments
				String[] defaults = getDefaultArgs(member
						.getChildren(ARG_ELEMENT_NAME));
				// Get any property elements
				Map properties = getProperties(member
						.getChildren(PROPERTY_ELEMENT_NAME));
				// Now make description instance
				ContainerTypeDescription scd = new ContainerTypeDescription(
						name, (IContainerInstantiator) exten, description,
						defaults, properties);
				IContainerFactory factory = ContainerFactory.getDefault();
				if (factory.containsDescription(scd)) {
					throw new CoreException(
							new Status(
									Status.ERROR,
									getDefault().getBundle().getSymbolicName(),
									FACTORY_NAME_COLLISION_ERRORCODE,
									NLS
											.bind(
													Messages.ECFPlugin_Container_Name_Collision_Prefix,
													name,
													extension
															.getExtensionPointUniqueIdentifier()),
									null));
				}
				// Now add the description and we're ready to go.
				factory.addDescription(scd);
				Trace.trace(ECFPlugin.PLUGIN_ID, ECFDebugOptions.DEBUG,
						method + ".added " + scd + " to factory " + factory); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (CoreException e) {
				logException(e.getStatus(), method, e);
			} catch (Exception e) {
				logException(
						new Status(
								Status.ERROR,
								getDefault().getBundle().getSymbolicName(),
								FACTORY_NAME_COLLISION_ERRORCODE,
								NLS
										.bind(
												Messages.ECFPlugin_Container_Name_Collision_Prefix,
												name,
												extension
														.getExtensionPointUniqueIdentifier()),
								null), method, e);
			}
		}
	}

	/**
	 * Setup container factory extension point
	 * 
	 * @param context
	 *            the BundleContext for this bundle
	 */
	protected void setupContainerFactoryExtensionPoint(BundleContext bc) {
		IExtensionRegistry reg = getExtensionRegistry();
		if (reg != null) {
			IExtensionPoint extensionPoint = reg
					.getExtensionPoint(CONTAINER_FACTORY_EPOINT);
			if (extensionPoint == null) {
				return;
			}
			addContainerFactoryExtensions(extensionPoint
					.getConfigurationElements());
		}
	}

	public IExtensionRegistry getExtensionRegistry() {
		return (IExtensionRegistry) extensionRegistryTracker.getService();
	}

	/**
	 * Setup start extension point
	 * 
	 * @param bc
	 *            the BundleContext fro this bundle
	 */
	protected void setupStartExtensionPoint(BundleContext bc) {
		IExtensionRegistry reg = getExtensionRegistry();
		if (reg != null) {
			IExtensionPoint extensionPoint = reg
					.getExtensionPoint(START_EPOINT);
			if (extensionPoint == null) {
				return;
			}
			runStartExtensions(extensionPoint.getConfigurationElements());
		}
	}

	protected void runStartExtensions(
			IConfigurationElement[] configurationElements) {
		String method = "runStartExtensions"; //$NON-NLS-1$
		// For each configuration element
		for (int m = 0; m < configurationElements.length; m++) {
			IConfigurationElement member = configurationElements[m];
			IECFStart exten = null;
			String name = null;
			try {
				// The only required attribute is "class"
				exten = (IECFStart) member
						.createExecutableExtension(CLASS_ATTRIBUTE);
				// Get name and get version, if available
				name = member.getAttribute(NAME_ATTRIBUTE);
				if (name == null)
					name = exten.getClass().getName();
				startExtension(name, exten);
			} catch (CoreException e) {
				logException(e.getStatus(), method, e);
			} catch (Exception e) {
				logException(new Status(Status.ERROR, getDefault().getBundle()
						.getSymbolicName(), START_ERRORCODE,
						"Unknown start exception", e), method, e); //$NON-NLS-1$
			}
		}
	}

	private void startExtension(String name, IECFStart exten) {
		// Create job to do start, and schedule
		ECFStartJob job = new ECFStartJob(name, exten);
		job.schedule();
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		plugin = this;
		this.context = context;
		this.extensionRegistryTracker = new ServiceTracker(context,
				IExtensionRegistry.class.getName(), null);
		this.extensionRegistryTracker.open();
		IExtensionRegistry registry = getExtensionRegistry();
		if (registry != null) {
			this.registryManager = new ECFRegistryManager();
			registry.addRegistryChangeListener(registryManager);
		}
		setupContainerFactoryExtensionPoint(context);
		setupStartExtensionPoint(context);
		containerFactoryServiceRegistration = context.registerService(
				IContainerFactory.class.getName(), ContainerFactory
						.getDefault(), null);
	}

	protected class ECFRegistryManager implements IRegistryChangeListener {
		public void registryChanged(IRegistryChangeEvent event) {
			IExtensionDelta delta[] = event.getExtensionDeltas(ECFNAMESPACE,
					CONTAINER_FACTORY_NAME);
			for (int i = 0; i < delta.length; i++) {
				switch (delta[i].getKind()) {
				case IExtensionDelta.ADDED:
					addContainerFactoryExtensions(delta[i].getExtension()
							.getConfigurationElements());
					break;
				case IExtensionDelta.REMOVED:
					removeContainerFactoryExtensions(delta[i].getExtension()
							.getConfigurationElements());
					break;
				}
			}
		}
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		fireDisposables();
		this.disposables = null;
		IExtensionRegistry reg = getExtensionRegistry();
		if (reg != null)
			reg.removeRegistryChangeListener(registryManager);
		this.registryManager = null;
		if (logServiceTracker != null) {
			logServiceTracker.close();
			logServiceTracker = null;
		}
		if (extensionRegistryTracker != null) {
			extensionRegistryTracker.close();
			extensionRegistryTracker = null;
		}
		if (containerFactoryServiceRegistration != null) {
			containerFactoryServiceRegistration.unregister();
			containerFactoryServiceRegistration = null;
		}
		this.context = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static ECFPlugin getDefault() {
		return plugin;
	}
}