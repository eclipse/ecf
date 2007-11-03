/****************************************************************************
 * Copyright (c) 20047 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.presence.collab.ui.url;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.Hashtable;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.datashare.AbstractShare;
import org.eclipse.ecf.datashare.IChannelContainerAdapter;
import org.eclipse.ecf.internal.presence.collab.ui.Activator;
import org.eclipse.ecf.internal.presence.collab.ui.Messages;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

/**
 * Send/receive requests to display an URL in the internal web browser.
 */
public class URLShare extends AbstractShare {

	private ID containerID = null;

	private static Hashtable urlsharechannels = new Hashtable();

	private static String FOO = "java.lang.NullPointerException\n\t\tat org.eclipse.ecf.provider.xmpp.XMPPContainer.isGoogle(XMPPContainer.java:288)\n" + "\t\tat org.eclipse.ecf.provider.xmpp.XMPPSContainer.createConnection(XMPPSContainer.java:50)\n" + "\t\tat org.eclipse.ecf.provider.generic.ClientSOContainer.connect(ClientSOContainer.java:151)\n" + "\t\tat org.eclipse.ecf.provider.xmpp.XMPPContainer.connect(XMPPContainer.java:176)\n" + "\t\tat org.eclipse.ecf.ui.actions.AsynchContainerConnectAction$AsynchActionJob.run(AsynchContainerConnectAction.java:127)\n" + "\t\tat org.eclipse.core.internal.jobs.Worker.run(Worker.java:55)\n";

	public static URLShare getURLShare(ID containerID) {
		return (URLShare) urlsharechannels.get(containerID);
	}

	public static URLShare addURLShare(ID containerID, IChannelContainerAdapter channelAdapter) throws ECFException {
		return (URLShare) urlsharechannels.put(containerID, new URLShare(containerID, channelAdapter));
	}

	public static URLShare removeURLShare(ID containerID) {
		return (URLShare) urlsharechannels.remove(containerID);
	}

	public URLShare(ID containerID, IChannelContainerAdapter adapter) throws ECFException {
		super(adapter);
		Assert.isNotNull(containerID);
		this.containerID = containerID;
	}

	protected ID getContainerID() {
		return containerID;
	}

	private void logError(String exceptionString, Throwable e) {
		Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, exceptionString, e));

	}

	private void logError(IStatus status) {
		Activator.getDefault().getLog().log(status);
	}

	private void showErrorToUser(String title, String message) {
		MessageDialog.openError(null, title, message);
	}

	private void showURL(final String user, final String url) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {

				if (MessageDialog.openQuestion(null, Messages.URLShare_RECEIVED_URL_TITLE, NLS.bind(Messages.URLShare_RECEIVED_URL_MESSAGE, user, url))) {
					final IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
					IWebBrowser browser;
					try {
						browser = support.createBrowser(null);
						browser.openURL(new URL(url));
					} catch (final Exception e) {
						showErrorToUser(Messages.URLShare_ERROR_BROWSER_TITLE, NLS.bind(Messages.URLShare_ERROR_BROWSER_MESSAGE, e.getLocalizedMessage()));
						logError(Messages.URLShare_EXCEPTION_LOG_BROWSER, e);
					}
				}
			}
		});
	}

	public void sendURL(final String senderuser, final ID toID, final String theURL) {
		try {
			sendMessage(toID, serialize(new Object[] {senderuser, theURL}));
		} catch (final ECFException e) {
			showErrorToUser(Messages.Share_ERROR_SEND_TITLE, NLS.bind(Messages.Share_ERROR_SEND_MESSAGE, e.getStatus().getException().getLocalizedMessage()));
			logError(e.getStatus());
		} catch (final Exception e) {
			showErrorToUser(Messages.Share_ERROR_SEND_TITLE, NLS.bind(Messages.Share_ERROR_SEND_MESSAGE, e.getLocalizedMessage()));
			logError(Messages.Share_EXCEPTION_LOG_SEND, e);
		}
	}

	public void showDialogAndSendURL(final String senderuser, final ID toID) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				final InputDialog input = new InputDialog(null, Messages.URLShare_INPUT_URL_DIALOG_TITLE, Messages.URLShare_ENTER_URL_DIALOG_TEXT, Messages.URLShare_ENTER_URL_DEFAULT_URL, null);
				input.setBlockOnOpen(true);
				final int result = input.open();
				if (result == InputDialog.OK) {
					final String send = input.getValue();
					if (send != null && !send.equals("")) { //$NON-NLS-1$
						try {
							sendMessage(toID, serialize(new Object[] {senderuser, send}));
						} catch (final ECFException e) {
							showErrorToUser(Messages.Share_ERROR_SEND_TITLE, NLS.bind(Messages.Share_ERROR_SEND_MESSAGE, e.getStatus().getException().getLocalizedMessage()));
							logError(e.getStatus());
						} catch (final Exception e) {
							showErrorToUser(Messages.Share_ERROR_SEND_TITLE, NLS.bind(Messages.Share_ERROR_SEND_MESSAGE, e.getLocalizedMessage()));
							logError(Messages.Share_EXCEPTION_LOG_SEND, e);
						}
					}
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.datashare.AbstractShare#handleChannelData(byte[])
	 */
	protected void handleMessage(ID fromContainerID, byte[] data) {
		try {
			final Object[] msg = (Object[]) deserialize(data);
			showURL((String) msg[0], (String) msg[1]);
		} catch (final Exception e) {
			showErrorToUser(Messages.Share_ERROR_RECEIVE_TITLE, NLS.bind(Messages.Share_ERROR_RECEIVE_MESSAGE, e.getLocalizedMessage()));
			logError(Messages.Share_EXCEPTION_LOG_MESSAGE, e);
		}
	}

	protected byte[] serialize(Object o) throws Exception {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(o);
		return bos.toByteArray();
	}

	protected Object deserialize(byte[] bytes) throws Exception {
		final ByteArrayInputStream bins = new ByteArrayInputStream(bytes);
		final ObjectInputStream oins = new ObjectInputStream(bins);
		return oins.readObject();
	}
}
