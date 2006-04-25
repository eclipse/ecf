/*******************************************************************************
 * Copyright (c) 2006 Ken Gilmer All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Ken Gilmer - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.example.collab.editor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.datashare.IChannel;
import org.eclipse.ecf.datashare.IChannelListener;
import org.eclipse.ecf.datashare.events.IChannelEvent;
import org.eclipse.ecf.datashare.events.IChannelMessageEvent;
import org.eclipse.ecf.example.collab.editor.message.SharedEditorSessionList;
import org.eclipse.ecf.example.collab.editor.message.SharedEditorSessionListRequest;

/**
 * This ECF listener waits for requests for information (such as any editor sessions
 * that the local client may have open). 
 * 
 * @author kg11212
 *
 */
public class PresenceChannelListener implements IChannelListener {

	private IChannel channel;
	
	public PresenceChannelListener() {
	}

	public void handleChannelEvent(IChannelEvent event) {
		if (channel == null) {
			channel = Activator.getDefault().getPresenceChannel();
		}
		
		if (event instanceof IChannelMessageEvent) {

			IChannelMessageEvent msg = (IChannelMessageEvent) event;

			ByteArrayInputStream bins = new ByteArrayInputStream(msg.getData());
			ObjectInputStream ois;
			try {
				ois = new ObjectInputStream(bins);
				Object o = ois.readObject();

				if (o instanceof SharedEditorSessionListRequest) {
					channel.sendMessage(createMessage(new SharedEditorSessionList(Activator.getDefault().getSessionNames())));
				}

			} catch (IOException e) {
				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, e.getLocalizedMessage(), e));
			} catch (ClassNotFoundException e) {
				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, e.getLocalizedMessage(), e));
			} catch (ECFException e) {
				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, e.getLocalizedMessage(), e));
			} finally {
				Activator.getDefault().setListenerActive(true);
				System.out.println("Setting events on");
			}
		}
	}
	
	public static byte[] createMessage(Object obj) throws IOException, ECFException {
		ByteArrayOutputStream bouts = new ByteArrayOutputStream();

		ObjectOutputStream douts = new ObjectOutputStream(bouts);
		douts.writeObject(obj);

		return bouts.toByteArray();
	}
}
