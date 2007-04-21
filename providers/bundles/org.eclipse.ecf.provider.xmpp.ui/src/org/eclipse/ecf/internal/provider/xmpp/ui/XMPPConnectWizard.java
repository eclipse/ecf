/****************************************************************************
 * Copyright (c) 2007 Remy Suen, Composent Inc., and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.internal.provider.xmpp.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerListener;
import org.eclipse.ecf.core.events.IContainerConnectedEvent;
import org.eclipse.ecf.core.events.IContainerEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.security.ConnectContextFactory;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.IExceptionHandler;
import org.eclipse.ecf.filetransfer.IFileTransferInfo;
import org.eclipse.ecf.filetransfer.IFileTransferListener;
import org.eclipse.ecf.filetransfer.IIncomingFileTransferRequestListener;
import org.eclipse.ecf.filetransfer.IOutgoingFileTransferContainerAdapter;
import org.eclipse.ecf.filetransfer.events.IFileTransferEvent;
import org.eclipse.ecf.filetransfer.events.IFileTransferRequestEvent;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveDoneEvent;
import org.eclipse.ecf.internal.ui.Messages;
import org.eclipse.ecf.presence.IIMMessageEvent;
import org.eclipse.ecf.presence.IIMMessageListener;
import org.eclipse.ecf.presence.IPresenceContainerAdapter;
import org.eclipse.ecf.presence.im.IChatManager;
import org.eclipse.ecf.presence.im.IChatMessage;
import org.eclipse.ecf.presence.im.IChatMessageEvent;
import org.eclipse.ecf.presence.im.IChatMessageSender;
import org.eclipse.ecf.presence.im.ITypingMessageEvent;
import org.eclipse.ecf.presence.im.ITypingMessageSender;
import org.eclipse.ecf.presence.ui.MessagesView;
import org.eclipse.ecf.presence.ui.MultiRosterView;
import org.eclipse.ecf.ui.IConnectWizard;
import org.eclipse.ecf.ui.actions.AsynchContainerConnectAction;
import org.eclipse.ecf.ui.dialogs.ContainerConnectErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

public class XMPPConnectWizard extends Wizard implements IConnectWizard {

	XMPPConnectWizardPage page;

	private Shell shell;

	private IContainer container;

	private ID targetID;

	private IConnectContext connectContext;

	protected IIncomingFileTransferRequestListener requestListener = new IIncomingFileTransferRequestListener() {
		public void handleFileTransferRequest(
				final IFileTransferRequestEvent event) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					String username = event.getRequesterID().getName();
					IFileTransferInfo transferInfo = event
							.getFileTransferInfo();
					String fileName = transferInfo.getFile().getName();
					Object[] bindings = new Object[] {
							username,
							fileName,
							((transferInfo.getFileSize() == -1) ? "unknown"
									: (transferInfo.getFileSize() + " bytes")),
							(transferInfo.getDescription() == null) ? "none"
									: transferInfo.getDescription() };
					if (MessageDialog.openQuestion(shell, NLS.bind(
							Messages.RosterView_ReceiveFile_title, username),
							NLS.bind(Messages.RosterView_ReceiveFile_message,
									bindings))) {
						FileDialog fd = new FileDialog(shell, SWT.OPEN);
						// XXX this should be some default path gotten from
						// preference. For now we'll have it be the user.home
						// system property
						fd.setFilterPath(System.getProperty("user.home"));
						fd.setFileName(fileName);
						int suffixLoc = fileName.lastIndexOf('.');
						if (suffixLoc != -1) {
							String ext = fileName.substring(fileName
									.lastIndexOf('.'));
							fd.setFilterExtensions(new String[] { ext });
						}
						fd.setText(NLS.bind(
								Messages.RosterView_ReceiveFile_filesavetitle,
								username));
						final String res = fd.open();
						if (res == null)
							event.reject();
						else {
							try {
								final FileOutputStream fos = new FileOutputStream(
										new File(res));
								event.accept(fos, new IFileTransferListener() {
									public void handleTransferEvent(
											IFileTransferEvent event) {
										// XXX Should have some some UI
										// for transfer events
										System.out
												.println("handleTransferEvent("
														+ event + ")");
										if (event instanceof IIncomingFileTransferReceiveDoneEvent) {
											try {
												fos.close();
											} catch (IOException e) {
											}
										}
									}
								});
							} catch (Exception e) {
								MessageDialog
										.openError(
												shell,
												Messages.RosterView_ReceiveFile_acceptexception_title,
												NLS
														.bind(
																Messages.RosterView_ReceiveFile_acceptexception_message,
																new Object[] {
																		fileName,
																		username,
																		e
																				.getLocalizedMessage() }));
							}
						}
					} else
						event.reject();
				}
			});
		}

	};

	public void addPages() {
		page = new XMPPConnectWizardPage();
		addPage(page);
	}

	public void init(IWorkbench workbench, IContainer container) {
		shell = workbench.getActiveWorkbenchWindow().getShell();
		this.container = container;
		this.workbench = workbench;
	}

	private IWorkbench workbench;
	private IChatMessageSender icms;
	private ITypingMessageSender itms;

	private void openView() {
		try {
			MultiRosterView view = (MultiRosterView) workbench
					.getActiveWorkbenchWindow().getActivePage().showView(
							MultiRosterView.VIEW_ID);
			view.addContainer(container);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	private void displayMessage(IChatMessageEvent e) {
		final IChatMessage message = e.getChatMessage();
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessagesView view = (MessagesView) workbench
						.getActiveWorkbenchWindow().getActivePage().findView(
								MessagesView.VIEW_ID);
				if (view != null) {
					IWorkbenchSiteProgressService service = (IWorkbenchSiteProgressService) view
							.getSite().getAdapter(
									IWorkbenchSiteProgressService.class);
					view.openTab(icms, itms, targetID, message.getFromID());
					view.showMessage(message.getFromID(), message.getBody());
					service.warnOfContentChange();
				} else {
					try {

						IWorkbenchPage page = workbench
								.getActiveWorkbenchWindow().getActivePage();
						view = (MessagesView) page.showView(
								MessagesView.VIEW_ID, null,
								IWorkbenchPage.VIEW_CREATE);
						if (!page.isPartVisible(view)) {
							IWorkbenchSiteProgressService service = (IWorkbenchSiteProgressService) view
									.getSite()
									.getAdapter(
											IWorkbenchSiteProgressService.class);
							service.warnOfContentChange();
						}
						view.openTab(icms, itms, targetID, message.getFromID());
						view
								.showMessage(message.getFromID(), message
										.getBody());
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	private void displayTypingNotification(final ITypingMessageEvent e) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessagesView view = (MessagesView) workbench
						.getActiveWorkbenchWindow().getActivePage().findView(
								MessagesView.VIEW_ID);
				if (view != null) {
					view.displayTypingNotification(e);
				}
			}
		});
	}

	public boolean performFinish() {
		connectContext = ConnectContextFactory
				.createPasswordConnectContext(page.getPassword());

		try {
			targetID = IDFactory.getDefault().createID(
					container.getConnectNamespace(), page.getConnectID());
		} catch (IDCreateException e) {
			// TODO: This needs to be handled properly
			e.printStackTrace();
			return false;
		}

		final IPresenceContainerAdapter adapter = (IPresenceContainerAdapter) container
				.getAdapter(IPresenceContainerAdapter.class);
		
		container.addListener(new IContainerListener() {
			public void handleEvent(IContainerEvent event) {
				if (event instanceof IContainerConnectedEvent) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							openView();
						}
					});
				}
			}
		});

		IChatManager icm = adapter.getChatManager();
		icms = icm.getChatMessageSender();
		itms = icm.getTypingMessageSender();

		icm.addMessageListener(new IIMMessageListener() {
			public void handleMessageEvent(IIMMessageEvent e) {
				if (e instanceof IChatMessageEvent) {
					displayMessage((IChatMessageEvent) e);
				} else if (e instanceof ITypingMessageEvent) {
					displayTypingNotification((ITypingMessageEvent) e);
				}
			}
		});

		IOutgoingFileTransferContainerAdapter ioftca = (IOutgoingFileTransferContainerAdapter) container
				.getAdapter(IOutgoingFileTransferContainerAdapter.class);
		ioftca.addListener(requestListener);

		new AsynchContainerConnectAction(container, targetID, connectContext,
				new IExceptionHandler() {
					public IStatus handleException(final Throwable exception) {
						if (exception != null) {
							exception.printStackTrace();
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									new ContainerConnectErrorDialog(workbench
											.getActiveWorkbenchWindow()
											.getShell(), 1, "See Details",
											targetID.getName(), exception)
											.open();
								}
							});
						}
						return Status.OK_STATUS;
					}

				}).run(null);

		return true;
	}

}
