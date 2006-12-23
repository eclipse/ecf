package org.eclipse.ecf.internal.presence.ui;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ecf.presence.ui";

	// The shared instance
	private static Activator plugin;

	private ImageRegistry registry = null;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
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
	public static Activator getDefault() {
		return plugin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#createImageRegistry()
	 */
	protected ImageRegistry createImageRegistry() {
		registry = super.createImageRegistry();

		registry.put(IImageFiles.DECORATION_USER_AVAILABLE, AbstractUIPlugin
				.imageDescriptorFromPlugin(PLUGIN_ID,
						IImageFiles.USER_AVAILABLE_ICON).createImage());

		registry.put(IImageFiles.DECORATION_USER_UNAVAILABLE, AbstractUIPlugin
				.imageDescriptorFromPlugin(PLUGIN_ID,
						IImageFiles.USER_UNAVAILABLE_ICON).createImage());

		registry.put(IImageFiles.DECORATION_USER_AWAY, AbstractUIPlugin
				.imageDescriptorFromPlugin(PLUGIN_ID,
						IImageFiles.USER_AWAY_ICON).createImage());

		registry.put(IImageFiles.DECORATION_USER_DND,
				AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID,
						IImageFiles.USER_DND_ICON).createImage());

		registry.put(IImageFiles.DECORATION_GROUP, AbstractUIPlugin
				.imageDescriptorFromPlugin(PLUGIN_ID, IImageFiles.GROUP_ICON)
				.createImage());
		return registry;
	}

}
