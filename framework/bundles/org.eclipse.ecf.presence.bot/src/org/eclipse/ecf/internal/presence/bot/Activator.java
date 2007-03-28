package org.eclipse.ecf.internal.presence.bot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.ecf.presence.bot.handler.ICommandHandler;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ecf.presence.bot";
	public static final String COMMAND_HANDLER_EPOINT_NAME = "commandHandler";
	public static final String COMMAND_HANDLER_EPOINT = PLUGIN_ID + "." + COMMAND_HANDLER_EPOINT_NAME;
	
	public static final String BOT_EPOINT_NAME = "bot";
	public static final String BOT_EPOINT = PLUGIN_ID + "." + BOT_EPOINT_NAME;
	
	// The shared instance
	private static Activator plugin;
	
	private BundleContext context = null;
	
	private ServiceTracker extensionRegistryTracker = null;

	private Map bots = new HashMap();
	private Map commands = new HashMap();

	/**
	 * The constructor
	 */
	public Activator() {
	}

	public IExtensionRegistry getExtensionRegistry() {
		if (extensionRegistryTracker == null) {
			this.extensionRegistryTracker = new ServiceTracker(context,
					IExtensionRegistry.class.getName(), null);
			this.extensionRegistryTracker.open();			
		}
		return (IExtensionRegistry) extensionRegistryTracker.getService();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		plugin = this;
		this.context = context;
		loadExtensions();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if (extensionRegistryTracker != null) {
			extensionRegistryTracker.close();
			extensionRegistryTracker = null;
		}
		plugin = null;
		this.context = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public Map getBots() {
		return this.bots;
	}


	private void loadExtensions() throws CoreException {
		// load the command handlers
		IExtensionRegistry reg = getExtensionRegistry();
		if (reg != null) {
			IConfigurationElement[] elements = reg.getConfigurationElementsFor(COMMAND_HANDLER_EPOINT);
			for(int i = 0; i < elements.length; i++) {
				String id = elements[i].getAttribute("botId");
				String expression = elements[i].getAttribute("expression");
				ICommandHandler handler = (ICommandHandler) elements[i].createExecutableExtension("class");
				List c = (List) commands.get(id);
				if(c == null) {
					c = new ArrayList();
					c.add(new CommandEntry(expression, handler));
					commands.put(id, c);
				} else {
					c.add(new CommandEntry(expression, handler));
					commands.put(id, c);
				}
			}
	
			// load the bots
			elements = reg.getConfigurationElementsFor(BOT_EPOINT);
			for(int i = 0; i < elements.length; i++) {
				String id = elements[i].getAttribute("id");
				String name = elements[i].getAttribute("name");
				String containerFactoryName = elements[i].getAttribute("containerFactoryName");
				String connectID = elements[i].getAttribute("connectID");
				String password = elements[i].getAttribute("password");
				String chatroom = elements[i].getAttribute("chatroom");
				List c = (List) commands.get(id);
				if(c == null)
					c = new ArrayList();
				IChatBotEntry bot = new ChatBotEntry(id, name, containerFactoryName, connectID, password, chatroom, c);
				bots.put(id, bot);
			}
		}

	}


}
