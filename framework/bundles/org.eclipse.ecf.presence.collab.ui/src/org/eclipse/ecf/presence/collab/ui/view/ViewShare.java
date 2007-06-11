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

package org.eclipse.ecf.presence.collab.ui.view;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

/**
 * Send/receive requests to share a specific view (identified by view ID).
 */
public class ViewShare extends AbstractShare {

	private ID containerID = null;

	public ViewShare(ID containerID, IChannelContainerAdapter adapter)
			throws ECFException {
		super(adapter);
		Assert.isNotNull(containerID);
		this.containerID = containerID;
	}

	protected ID getContainerID() {
		return containerID;
	}

	private void logError(String exceptionString, Throwable e) {
		Activator.getDefault().getLog().log(
				new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR,
						exceptionString, e));

	}

	private void logError(IStatus status) {
		Activator.getDefault().getLog().log(status);
	}

	private void showErrorToUser(String title, String message) {
		MessageDialog.openError(null, title, message);
	}

	private void showView(final String user, final String viewID,
			final String secondaryID, final int mode) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				// Ask user if they want to display view.
				if (MessageDialog
						.openQuestion(
								null,
								"Received Open View Request",
								NLS
										.bind(
												"Received open view request from {0}.  Allow view to open?",
												user))) {
					try {
						IWorkbenchWindow ww = PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow();
						IWorkbenchPage wp = ww.getActivePage();
						if (wp == null)
							throw new PartInitException(
									"workbench page is null");
						// Actually show view requested
						wp.showView(viewID, secondaryID, mode);

					} catch (Exception e) {
						showErrorToUser("Error opening view", NLS.bind(
								"Error opening view {0}", e
										.getLocalizedMessage()));
						logError("Exception in openView", e);
					}
				}
			}
		});
	}

	public void sendOpenViewRequestMessage(final String senderuser, final ID toID) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow ww = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow();
				IWorkbenchPage page = ww.getActivePage();
				if (page == null)
					return;
				ElementTreeSelectionDialog dlg = new ElementTreeSelectionDialog(
						null, new ShowViewDialogLabelProvider(),
						new ShowViewDialogTreeContentProvider());
				dlg.setTitle("Send Show View Request");
				dlg.setMessage("Select View to Open Remotely");
				dlg.addFilter(new ShowViewDialogViewerFilter());
				dlg.setComparator(new ViewerComparator());
				dlg.setValidator(new ISelectionStatusValidator() {
					public IStatus validate(Object[] selection) {
						for (int i = 0; i < selection.length; ++i)
							if (!(selection[i] instanceof IViewDescriptor))
								return new Status(Status.ERROR,
										Activator.PLUGIN_ID, 0, "", null);

						return new Status(Status.OK, Activator.getDefault()
								.getBundle().getSymbolicName(), 0, "", null);
					}
				});
				IViewRegistry reg = PlatformUI.getWorkbench().getViewRegistry();
				dlg.setInput(reg);
				dlg.open();
				if (dlg.getReturnCode() == Window.CANCEL)
					return;

				Object[] descs = dlg.getResult();
				if (descs == null)
					return;

				String[] selectedIDs = new String[descs.length];
				for (int i = 0; i < descs.length; ++i) {
					selectedIDs[i] = ((IViewDescriptor) descs[i]).getId();
					try {
						// Actually send messages to target remote user (toID),
						// with selectedIDs (view IDs) to show
						sendMessage(toID, serialize(new Object[] { senderuser,
								selectedIDs[i] }));
					} catch (ECFException e) {
						showErrorToUser(Messages.Share_ERROR_SEND_TITLE, NLS
								.bind(Messages.Share_ERROR_SEND_MESSAGE, e
										.getStatus().getException()
										.getLocalizedMessage()));
						logError(e.getStatus());
					} catch (Exception e) {
						showErrorToUser(Messages.Share_ERROR_SEND_TITLE, NLS
								.bind(Messages.Share_ERROR_SEND_MESSAGE, e
										.getLocalizedMessage()));
						logError(Messages.Share_EXCEPTION_LOG_SEND, e);
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
			Object[] msg = (Object[]) deserialize(data);
			showView((String) msg[0], (String) msg[1], null,
					IWorkbenchPage.VIEW_ACTIVATE);
		} catch (Exception e) {
			showErrorToUser(Messages.Share_ERROR_RECEIVE_TITLE, NLS.bind(
					Messages.Share_ERROR_RECEIVE_MESSAGE, e
							.getLocalizedMessage()));
			logError(Messages.Share_EXCEPTION_LOG_MESSAGE, e);
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
