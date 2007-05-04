/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.presence.collab.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.datashare.AbstractShare;
import org.eclipse.ecf.datashare.IChannelContainerAdapter;
import org.eclipse.ecf.internal.presence.collab.ui.Messages;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

/**
 * 
 */
public class URLShare extends AbstractShare {

	private ID containerID = null;

	public URLShare(ID containerID, IChannelContainerAdapter adapter)
			throws ECFException {
		super(adapter);
		Assert.isNotNull(containerID);
		this.containerID = containerID;
		URLShareRosterContributionItem.addURLShare(containerID, this);
	}

	protected ID getContainerID() {
		return containerID;
	}

	private void showURL(final String user, final String url) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (MessageDialog.openQuestion(null, Messages.URLShare_RECEIVED_URL_TITLE, NLS.bind(
						Messages.URLShare_RECEIVED_URL_MESSAGE,
						user))) {
					IWorkbenchBrowserSupport support = PlatformUI.getWorkbench()
							.getBrowserSupport();
					IWebBrowser browser;
					try {
						browser = support.createBrowser(null);
						browser.openURL(new URL(url));
					} catch (Exception e) {
						// TODO display error to user
						e.printStackTrace();
					}
				}
			}
		});
	}

	public void sendURL(final String senderuser, final ID toID) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				InputDialog input = new InputDialog(null,
						Messages.URLShare_INPUT_URL_DIALOG_TITLE,
						Messages.URLShare_ENTER_URL_DIALOG_TEXT,
						Messages.URLShare_ENTER_URL_DEFAULT_URL, null);
				input.setBlockOnOpen(true);
				int result = input.open();
				if (result == InputDialog.OK) {
					String send = input.getValue();
					if (send != null && !send.equals("")) { //$NON-NLS-1$
						try {
							sendMessage(toID, serialize(new Object[] {
									senderuser, send }));
						} catch (Exception e) {
							// TODO display error to user
						}
					}
				}
			}
		});
	}

	public synchronized void dispose() {
		super.dispose();
		URLShareRosterContributionItem.removeURLShare(containerID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.datashare.AbstractShare#handleChannelData(byte[])
	 */
	protected void handleMessage(ID fromContainerID, byte[] data) {
		try {
			Object[] msg = (Object[]) deserialize(data);
			showURL((String) msg[0], (String) msg[1]);
		} catch (Exception e) {
			// XXX show and/or log error
		}
	}

	protected byte[] serialize(Object o) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(o);
		return bos.toByteArray();
	}

	protected Object deserialize(byte[] bytes) throws Exception {
		ByteArrayInputStream bins = new ByteArrayInputStream(bytes);
		ObjectInputStream oins = new ObjectInputStream(bins);
		return oins.readObject();
	}
}
