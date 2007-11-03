/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.presence.collab.ui.console;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.TextConsole;

/**
 * Send/receive requests to share a specific view (identified by view ID).
 */
public class StackShare extends AbstractShare {

	private static Hashtable stackSharechannels = new Hashtable();

	private ID containerID = null;

	private static TextSelection selection = null;
	private static boolean initialized = false;

	private static final ISelectionListener selectionListener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (part instanceof IConsoleView && selection instanceof TextSelection) {
				StackShare.selection = (TextSelection) selection;
			}
		}
	};

	public static StackShare getStackShare(ID containerID) {
		return (StackShare) stackSharechannels.get(containerID);
	}

	public static StackShare addStackShare(ID containerID, IChannelContainerAdapter channelAdapter) throws ECFException {
		initialize();
		return (StackShare) stackSharechannels.put(containerID, new StackShare(containerID, channelAdapter));
	}

	private static void initialize() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				if (!initialized) {
					final IWorkbenchWindow ww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					final IWorkbenchPage page = ww.getActivePage();
					page.addSelectionListener(selectionListener);
					initialized = true;
				}
			}
		});
	}

	public static TextSelection getSelection() {
		return selection;
	}

	public static StackShare removeStackShare(ID containerID) {
		return (StackShare) stackSharechannels.remove(containerID);
	}

	public StackShare(ID containerID, IChannelContainerAdapter adapter) throws ECFException {
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

	private void handleShowStackRequest(final String user, final String stackTrace) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					final String viewID = "org.eclipse.ui.console.ConsoleView"; //$NON-NLS-1$
					final IWorkbenchWindow ww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if (ww == null)
						throw new PartInitException(Messages.StackShare_EXCEPTION_WW_NOT_AVAILABLE);
					final IWorkbenchPage wp = ww.getActivePage();
					if (wp == null)
						throw new PartInitException(Messages.StackShare_EXCEPTION_WP_NOT_AVAILABLE);
					wp.showView(viewID);
					final IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
					final IConsole[] consoles = consoleManager.getConsoles();
					if (consoles.length == 0) {
						MessageDialog.openInformation(null, NLS.bind(Messages.StackShare_STACK_TRACE_FROM_TITLE, user), NLS.bind(Messages.StackShare_STACK_TRACE_FROM_MESSAGE, user));
						return;
					} else {
						for (int i = 0; i < consoles.length; i++) {
							final String consoleType = consoles[i].getType();
							if (consoleType != null && consoleType.equals("javaStackTraceConsole")) { //$NON-NLS-1$
								final TextConsole textConsole = (TextConsole) consoles[i];
								textConsole.activate();
								final IDocument document = textConsole.getDocument();
								final String text = document.get() + getStackTraceToShow(user, stackTrace);
								document.set(text);
							}
						}
					}
				} catch (final Exception e) {
					showErrorToUser(Messages.StackShare_STACKSHARE_ERROR_DIALOG_TITLE, NLS.bind(Messages.StackShare_STACKSHARE_ERROR_DIALOG_MESSAGE, e.getLocalizedMessage()));
					logError(Messages.StackShare_STACKSHARE_ERROR_LOG_MESSAGE, e);
				}
			}
		});
	}

	private String getStackTraceToShow(String user, String stackTrace) {
		return NLS.bind(Messages.StackShare_STACK_TRACE_CONTENT, user, stackTrace);
	}

	public void sendShareStackRequest(final String senderuser, final ID toID, final String stackTrace) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {

				try {
					// Actually send messages to target remote user (toID),
					// with selectedIDs (view IDs) to show
					sendMessage(toID, serialize(new Object[] {senderuser, stackTrace}));
				} catch (final ECFException e) {
					showErrorToUser(Messages.Share_ERROR_SEND_TITLE, NLS.bind(Messages.Share_ERROR_SEND_MESSAGE, e.getStatus().getException().getLocalizedMessage()));
					logError(e.getStatus());
				} catch (final Exception e) {
					showErrorToUser(Messages.Share_ERROR_SEND_TITLE, NLS.bind(Messages.Share_ERROR_SEND_MESSAGE, e.getLocalizedMessage()));
					logError(Messages.Share_EXCEPTION_LOG_SEND, e);
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
			handleShowStackRequest((String) msg[0], (String) msg[1]);
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
