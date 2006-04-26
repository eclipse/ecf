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
import java.io.IOException;
import java.io.ObjectInputStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.datashare.IChannelListener;
import org.eclipse.ecf.datashare.events.IChannelEvent;
import org.eclipse.ecf.datashare.events.IChannelMessageEvent;
import org.eclipse.ecf.example.collab.editor.message.EditorChangeMessage;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class EditChannelListener implements IChannelListener {

	private IDocument document;

	private AbstractTextEditor editor;

	private StyledText textControl;

	public EditChannelListener(IDocument document, AbstractTextEditor editor) {
		this.document = document;
		this.editor = editor;
		textControl = (StyledText) editor.getAdapter(Control.class);
		
		System.out.println(textControl.getAlignment());
	}

	public void handleChannelEvent(IChannelEvent event) {
		if (event instanceof IChannelMessageEvent) {
			System.out.println("Receiving");
			setEditorEditable(false);
			Activator.getDefault().setListenerActive(false);

			IChannelMessageEvent msg = (IChannelMessageEvent) event;

			ByteArrayInputStream bins = new ByteArrayInputStream(msg.getData());
			ObjectInputStream ois;
			try {
				ois = new ObjectInputStream(bins);
				EditorChangeMessage message = (EditorChangeMessage) ois
						.readObject();

				// Append text from remote to end of document
				appendLocallyFromRemote(message);
			} catch (IOException e) {
				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, e.getLocalizedMessage(), e));
			} catch (ClassNotFoundException e) {
				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, e.getLocalizedMessage(), e));
			} finally {
				setEditorEditable(true);
				Activator.getDefault().setListenerActive(true);
			}
		}
	}

	protected void appendLocallyFromRemote(final EditorChangeMessage message) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				// Crude way of ignoring duplicate messages
				if (message.getDocument().equals(document.get())) {
					return;
				}

				ISelection selection = editor.getSelectionProvider()
						.getSelection();
				if (selection instanceof TextSelection) {
					TextSelection textSelection = (TextSelection) selection;
					document.set(message.getDocument());
					editor.selectAndReveal(textSelection.getOffset(), 0);
				}
			}
		});
	}

	private void setEditorEditable(final boolean editable) {
		if (!textControl.isDisposed()) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					textControl.setEditable(editable);
				}
			});
		}
	}
}
