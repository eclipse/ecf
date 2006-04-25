package org.eclipse.ecf.example.collab.editor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.datashare.IChannel;
import org.eclipse.ecf.datashare.IChannelListener;
import org.eclipse.ecf.datashare.events.IChannelEvent;
import org.eclipse.ecf.datashare.events.IChannelMessageEvent;
import org.eclipse.ecf.example.collab.editor.message.EditorChangeMessage;
import org.eclipse.ecf.example.collab.editor.message.SharedEditorSessionList;
import org.eclipse.ecf.example.collab.editor.message.SharedEditorSessionListRequest;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.AbstractTextEditor;

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
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ECFException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
