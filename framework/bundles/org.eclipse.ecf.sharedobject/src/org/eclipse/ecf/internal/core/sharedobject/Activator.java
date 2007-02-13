package org.eclipse.ecf.internal.core.sharedobject;

import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.sharedobject.ISharedObjectFactory;
import org.eclipse.ecf.core.sharedobject.SharedObjectFactory;
import org.eclipse.ecf.core.sharedobject.SharedObjectTypeDescription;
import org.eclipse.ecf.core.sharedobject.provider.ISharedObjectInstantiator;
import org.eclipse.ecf.core.util.Trace;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ecf.sharedobject"; //$NON-NLS-1$

	protected static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$

	protected static final String NAME_ATTRIBUTE = "name"; //$NON-NLS-1$

	protected static final String PROPERTY_ELEMENT_NAME = "property"; //$NON-NLS-1$

	protected static final String VALUE_ATTRIBUTE = "value"; //$NON-NLS-1$

	protected static final String NAMESPACE_NAME = "sharedObjectFactory"; //$NON-NLS-1$

	protected static final String SHAREDOBJECT_FACTORY_EPOINT = PLUGIN_ID + "." //$NON-NLS-1$
			+ NAMESPACE_NAME;

	protected static final String DESCRIPTION_ATTRIBUTE = "description"; //$NON-NLS-1$

	private static final int REMOVE_SHAREDOBJECT_ERRORCODE = 1001;

	private static final int FACTORY_NAME_COLLISION_ERRORCODE = 2001;

	// The shared instance
	private static Activator plugin;

	private IRegistryChangeListener registryManager = null;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		setupSharedObjectExtensionPoint(context);
		this.registryManager = new SharedObjectRegistryManager();
		Platform.getExtensionRegistry().addRegistryChangeListener(
				registryManager);
		Trace.exiting(Activator.getDefault(),
				SharedObjectDebugOptions.METHODS_ENTERING, Activator.class,
				"start"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		Trace.entering(Activator.getDefault(),
				SharedObjectDebugOptions.METHODS_EXITING, Activator.class,
				"stop"); //$NON-NLS-1$
		Platform.getExtensionRegistry().removeRegistryChangeListener(
				registryManager);
		registryManager = null;
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Remove extensions for shared object extension point
	 * 
	 * @param members
	 *            the members to remove
	 */
	protected void removeSharedObjectExtensions(IConfigurationElement[] members) {
		for (int m = 0; m < members.length; m++) {
			IConfigurationElement member = members[m];
			String name = null;
			try {
				name = member.getAttribute(NAME_ATTRIBUTE);
				if (name == null) {
					name = member.getAttribute(CLASS_ATTRIBUTE);
				}
				if (name == null)
					continue;
				ISharedObjectFactory factory = SharedObjectFactory.getDefault();
				SharedObjectTypeDescription sd = factory
						.getDescriptionByName(name);
				if (sd == null || !factory.containsDescription(sd)) {
					continue;
				}
				// remove
				factory.removeDescription(sd);
				org.eclipse.ecf.core.util.Trace.trace(Activator.getDefault(),
						SharedObjectDebugOptions.DEBUG,
						"removeSharedObjectExtensions.removedDescription(" + sd //$NON-NLS-1$
								+ ")"); //$NON-NLS-1$
			} catch (Exception e) {
				org.eclipse.ecf.core.util.Trace.catching(
						Activator.getDefault(),
						SharedObjectDebugOptions.EXCEPTIONS_CATCHING,
						Activator.class, "removeSharedObjectExtensions", e); //$NON-NLS-1$
				getDefault()
						.getLog()
						.log(
								new Status(
										IStatus.ERROR,
										Activator.PLUGIN_ID,
										REMOVE_SHAREDOBJECT_ERRORCODE,
										Messages.Activator_Exception_Removing_Extension,
										e));
			}
		}
	}

	/**
	 * Add shared object extension point extensions
	 * 
	 * @param members
	 *            to add
	 */
	protected void addSharedObjectExtensions(IConfigurationElement[] members) {
		String bundleName = getDefault().getBundle().getSymbolicName();
		// For each configuration element
		for (int m = 0; m < members.length; m++) {
			IConfigurationElement member = members[m];
			// Get the label of the extender plugin and the ID of the extension.
			IExtension extension = member.getDeclaringExtension();
			ISharedObjectInstantiator exten = null;
			String name = null;
			try {
				// The only required attribute is "class"
				exten = (ISharedObjectInstantiator) member
						.createExecutableExtension(CLASS_ATTRIBUTE);
				name = member.getAttribute(NAME_ATTRIBUTE);
				if (name == null) {
					name = member.getAttribute(CLASS_ATTRIBUTE);
				}
				// Get description, if present
				String description = member.getAttribute(DESCRIPTION_ATTRIBUTE);
				if (description == null) {
					description = ""; //$NON-NLS-1$
				}
				// Get any property elements
				Map properties = getProperties(member
						.getChildren(PROPERTY_ELEMENT_NAME));
				// Now make description instance
				SharedObjectTypeDescription scd = new SharedObjectTypeDescription(
						name, exten, description, properties);
				org.eclipse.ecf.core.util.Trace.trace(Activator.getDefault(),
						SharedObjectDebugOptions.DEBUG,
						"setupSharedObjectExtensionPoint:createdDescription(" //$NON-NLS-1$
								+ scd + ")"); //$NON-NLS-1$
				ISharedObjectFactory factory = SharedObjectFactory.getDefault();
				if (factory.containsDescription(scd))
					throw new CoreException(
							new Status(
									Status.ERROR,
									bundleName,
									FACTORY_NAME_COLLISION_ERRORCODE,
									"name=" //$NON-NLS-1$
											+ name
											+ ";extension point id=" //$NON-NLS-1$
											+ extension
													.getExtensionPointUniqueIdentifier(),
									null));

				// Now add the description and we're ready to go.
				factory.addDescription(scd);
				org.eclipse.ecf.core.util.Trace.trace(Activator.getDefault(),
						SharedObjectDebugOptions.DEBUG,
						"setupSharedObjectExtensionPoint.addedDescriptionToFactory(" //$NON-NLS-1$
								+ scd + ")"); //$NON-NLS-1$
			} catch (CoreException e) {
				getDefault().getLog().log(e.getStatus());
				org.eclipse.ecf.core.util.Trace.catching(
						Activator.getDefault(),
						SharedObjectDebugOptions.EXCEPTIONS_CATCHING,
						Activator.class, "addSharedObjectExtensions", e); //$NON-NLS-1$
			} catch (Exception e) {
				getDefault()
						.getLog()
						.log(
								new Status(
										Status.ERROR,
										bundleName,
										FACTORY_NAME_COLLISION_ERRORCODE,
										"name=" //$NON-NLS-1$
												+ name
												+ ";extension point id=" //$NON-NLS-1$
												+ extension
														.getExtensionPointUniqueIdentifier(),
										null));
				org.eclipse.ecf.core.util.Trace.catching(
						Activator.getDefault(),
						SharedObjectDebugOptions.EXCEPTIONS_CATCHING,
						Activator.class, "addSharedObjectExtensions", e); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Setup shared object extension point
	 * 
	 * @param context
	 *            the BundleContext for this bundle
	 */
	protected void setupSharedObjectExtensionPoint(BundleContext bc) {
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = reg
				.getExtensionPoint(SHAREDOBJECT_FACTORY_EPOINT);
		if (extensionPoint == null) {
			return;
		}
		addSharedObjectExtensions(extensionPoint.getConfigurationElements());
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

	protected class SharedObjectRegistryManager implements
			IRegistryChangeListener {
		public void registryChanged(IRegistryChangeEvent event) {
			IExtensionDelta delta[] = event.getExtensionDeltas(PLUGIN_ID,
					NAMESPACE_NAME);
			for (int i = 0; i < delta.length; i++) {
				switch (delta[i].getKind()) {
				case IExtensionDelta.ADDED:
					addSharedObjectExtensions(delta[i].getExtension()
							.getConfigurationElements());
					break;
				case IExtensionDelta.REMOVED:
					removeSharedObjectExtensions(delta[i].getExtension()
							.getConfigurationElements());
					break;
				}
			}
		}
	}

}
