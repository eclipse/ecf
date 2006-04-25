/*******************************************************************************
 * Copyright (c) 2006 Ken Gilmer All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Ken Gilmer - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.example.collab.editor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.datashare.IChannel;
import org.eclipse.ecf.datashare.IChannelContainer;
import org.eclipse.ecf.datashare.IChannelListener;
import org.eclipse.ecf.example.collab.editor.message.EditorChangeMessage;
import org.eclipse.ecf.example.collab.editor.preferences.ClientPreferencePage;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class EditorListener implements IDocumentListener {
	public static final String SESSION_NAME_DELIMITER = "_";

	private IDocument document;

	private AbstractTextEditor editor;

	private IChannel channel;

	private IContainer container = null;

	private IChannelListener channelListener;

	private String sessionID;

	public EditorListener(IDocument document, AbstractTextEditor textEditor) {
		this.document = document;
		this.editor = textEditor;

		try {
			intializeEditorSession();
			
			if (Activator.getDefault().getPresenceChannelListener() == null) {
				Activator.getDefault().intializePresenceSession(new PresenceChannelListener());
			}
		} catch (ECFException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, e.getLocalizedMessage(), e));
		}
	}

	public void documentAboutToBeChanged(DocumentEvent event) {

	}

	public void documentChanged(DocumentEvent event) {
		if (!Activator.getDefault().isListenerActive()) {
			return;
		}

		try {
			IDocument newDocument = event.getDocument();

			if (channel == null) {
				// Communication error has occured. Stop listening to
				// document.
				document.removeDocumentListener(this);
			}

			channel.sendMessage(createMessageFromEvent(event));

			this.document = newDocument;
		} catch (ECFException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, e.getLocalizedMessage(), e));
		} catch (IOException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, e.getLocalizedMessage(), e));
		}
	}

	private byte[] createMessageFromEvent(DocumentEvent event) throws IOException, ECFException {
		ByteArrayOutputStream bouts = new ByteArrayOutputStream();
		ObjectOutputStream douts = new ObjectOutputStream(bouts);
		douts.writeObject(new EditorChangeMessage(event.getDocument().get()));
		return bouts.toByteArray();
	}

	public void intializeEditorSession() throws ECFException {
		container = ContainerFactory.getDefault().createContainer(
				Activator.getDefault().getPreferenceStore().getString(ClientPreferencePage.CONTAINER_TYPE));

		IChannelContainer channelContainer = (IChannelContainer) container.getAdapter(IChannelContainer.class);

		sessionID = Activator.getDefault().getPreferenceStore().getString(ClientPreferencePage.CHANNEL_ID) + SESSION_NAME_DELIMITER + editor.getTitle();

		Activator.getDefault().addSession(sessionID, editor.getTitle());
		
		final ID channelID = IDFactory.getDefault().createID(channelContainer.getChannelNamespace(), sessionID);

		channelListener = new EditChannelListener(document, editor);

		channel = channelContainer.createChannel(channelID, channelListener, new HashMap());

		container.connect(IDFactory.getDefault().createID(container.getConnectNamespace(),
				Activator.getDefault().getPreferenceStore().getString(ClientPreferencePage.TARGET_SERVER)), null);
	}

	

	public String getSessionID() {
		return sessionID;
	}
}
