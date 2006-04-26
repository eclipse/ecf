/*******************************************************************************
 * Copyright (c) 2006 Ken Gilmer. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Ken Gilmer - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.example.collab.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.datashare.IChannel;
import org.eclipse.ecf.datashare.IChannelContainer;
import org.eclipse.ecf.datashare.IChannelListener;
import org.eclipse.ecf.example.collab.editor.message.SharedEditorSessionList;
import org.eclipse.ecf.example.collab.editor.model.SessionInstance;
import org.eclipse.ecf.example.collab.editor.preferences.ClientPreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class Activator extends AbstractUIPlugin {

	// The shared instance.
	private static Activator plugin;

	private static boolean listenerActive = true;

	private List sessionNames;

	private IChannelListener presenceChannelListener = null;

	private IChannel presenceChannel;

	private IContainer presenceContainer = null;
	
	public static final String PLUGIN_ID = "org.eclipse.ecf.example.collab.editor";

	/**
	 * The constructor.
	 */
	public Activator() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);

		// Make sure the defaults get created.
		ClientPreferencePage p = new ClientPreferencePage();
		p.initializeDefaults();

		sessionNames = new ArrayList();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared instance.
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ecf.example.collab.editor", path);
	}

	public boolean isListenerActive() {
		return listenerActive;
	}

	synchronized public void setListenerActive(boolean active) {
		listenerActive = active;
	}

	/*
	 * public List getSessionNames() { return sessionNames; }
	 */

	public void addSession(String channelID, String sessionName) {
		
		sessionNames.add(new SessionInstance(channelID, sessionName, getPreferenceStore().getString(ClientPreferencePage.LOCAL_NAME), Calendar.getInstance().getTime()));

		if (presenceChannel != null) {
			// Tell everyone there is a new shared editor.
			try {
				presenceChannel.sendMessage((new SharedEditorSessionList(sessionNames)).toByteArray());
			} catch (ECFException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public IChannelListener getPresenceChannelListener() {
		return presenceChannelListener;
	}

	public IChannel intializePresenceSession(IChannelListener clistener) throws ECFException {
		presenceContainer = ContainerFactory.getDefault().createContainer(
				Activator.getDefault().getPreferenceStore().getString(ClientPreferencePage.CONTAINER_TYPE));

		IChannelContainer channelContainer = (IChannelContainer) presenceContainer.getAdapter(IChannelContainer.class);

		final ID channelID = IDFactory.getDefault().createID(channelContainer.getChannelNamespace(),
				Activator.getDefault().getPreferenceStore().getString(ClientPreferencePage.CHANNEL_ID) + ".presence");

		presenceChannel = channelContainer.createChannel(channelID, clistener, new HashMap());

		this.presenceChannelListener = clistener;

		presenceContainer.connect(IDFactory.getDefault().createID(presenceContainer.getConnectNamespace(),
				Activator.getDefault().getPreferenceStore().getString(ClientPreferencePage.TARGET_SERVER)), null);

		return presenceChannel;
	}

	public IChannel getPresenceChannel() {
		return presenceChannel;
	}

	public List getSessionNames() {
		return sessionNames;
	}
}
