package org.eclipse.ecf.example.collab.editor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;

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
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class EditorListener implements IDocumentListener {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

		sessionID = Activator.getDefault().getPreferenceStore().getString(ClientPreferencePage.CHANNEL_ID) + "_" + editor.getTitle();

		Activator.getDefault().addSession(sessionID);
		
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
