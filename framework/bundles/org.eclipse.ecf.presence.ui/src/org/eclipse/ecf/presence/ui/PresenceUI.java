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
package org.eclipse.ecf.presence.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerListener;
import org.eclipse.ecf.core.events.IContainerConnectedEvent;
import org.eclipse.ecf.core.events.IContainerDisconnectedEvent;
import org.eclipse.ecf.core.events.IContainerEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainer;
import org.eclipse.ecf.core.user.IUser;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.internal.presence.ui.Activator;
import org.eclipse.ecf.presence.IAccountManager;
import org.eclipse.ecf.presence.IIMMessageEvent;
import org.eclipse.ecf.presence.IIMMessageListener;
import org.eclipse.ecf.presence.IPresence;
import org.eclipse.ecf.presence.IPresenceContainerAdapter;
import org.eclipse.ecf.presence.IPresenceListener;
import org.eclipse.ecf.presence.IPresenceSender;
import org.eclipse.ecf.presence.Presence;
import org.eclipse.ecf.presence.im.IChatManager;
import org.eclipse.ecf.presence.im.IChatMessageEvent;
import org.eclipse.ecf.presence.im.IChatMessageSender;
import org.eclipse.ecf.presence.im.ITypingMessageEvent;
import org.eclipse.ecf.presence.im.ITypingMessageSender;
import org.eclipse.ecf.presence.roster.IRosterEntry;
import org.eclipse.ecf.presence.roster.IRosterSubscriptionListener;
import org.eclipse.ecf.presence.roster.IRosterSubscriptionSender;
import org.eclipse.ecf.ui.dialogs.ReceiveAuthorizeRequestDialog;
import org.eclipse.ecf.ui.views.ILocalInputHandler;
import org.eclipse.ecf.ui.views.RosterView;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Presence user interface
 */
public class PresenceUI {

	protected static final String ROSTER_VIEW_ID = "org.eclipse.ecf.ui.view.rosterview";

	protected static final int SEND_ERRORCODE = IStatus.ERROR;

	protected RosterView rosterView = null;

	protected IPresenceSender presenceSender = null;

	protected IAccountManager accountManager = null;

	protected IRosterSubscriptionSender rosterSubscriptionSender = null;

	protected IPresenceContainerAdapter presenceAdapter = null;

	protected ISharedObjectContainer soContainer = null;

	protected IUser localUser = null;

	protected ID groupID = null;

	protected IContainer container;

	protected IChatManager chatManager;

	protected IChatMessageSender chatMessageSender;

	protected ITypingMessageSender typingMessageSender;

	public PresenceUI(IContainer container,
			IPresenceContainerAdapter presenceAdapter) {
		this.container = container;
		this.presenceAdapter = presenceAdapter;
		this.presenceSender = this.presenceAdapter.getRosterManager()
				.getPresenceSender();
		this.rosterSubscriptionSender = this.presenceAdapter.getRosterManager()
				.getRosterSubscriptionSender();
		this.accountManager = this.presenceAdapter.getAccountManager();
		this.chatManager = this.presenceAdapter.getChatManager();
		this.chatMessageSender = this.chatManager.getChatMessageSender();
		this.typingMessageSender = this.chatManager.getTypingMessageSender();
		this.soContainer = (ISharedObjectContainer) this.container
				.getAdapter(ISharedObjectContainer.class);
		
		this.groupID = container.getConnectedID();
	}

	public void showForUser(final IUser localUser) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				try {
					IWorkbenchWindow ww = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow();
					IWorkbenchPage wp = ww.getActivePage();

					IViewPart view = wp.showView(ROSTER_VIEW_ID);
					rosterView = (RosterView) view;

					PresenceUI.this.localUser = localUser;

				} catch (Exception e) {
					Activator.getDefault().getLog().log(
							new Status(IStatus.ERROR, Activator.PLUGIN_ID,
									SEND_ERRORCODE,
									"Exception showing presence view", e));
				}
			}
		});

		chatManager.addMessageListener(new IIMMessageListener() {
			public void handleMessageEvent(
					final IIMMessageEvent chatMessageEvent) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						if (chatMessageEvent instanceof IChatMessageEvent) {
							rosterView.handleMessageEvent(chatMessageEvent);
						} else if (chatMessageEvent instanceof ITypingMessageEvent) {
							rosterView.handleTyping(chatMessageEvent
									.getFromID());
						}
					}
				});
			}
		});

		container.addListener(new IContainerListener() {
			public void handleEvent(IContainerEvent event) {
				if (event instanceof IContainerConnectedEvent) {
					IContainerConnectedEvent cce = (IContainerConnectedEvent) event;
					final ID joinedContainer = cce.getTargetID();
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							ILocalInputHandler handler = new ILocalInputHandler() {
								public void inputText(ID userID, String text) {
									try {
										if (chatMessageSender != null)
											chatMessageSender.sendChatMessage(
													userID, text);
									} catch (ECFException e) {
										Activator
												.getDefault()
												.getLog()
												.log(
														new Status(
																IStatus.ERROR,
																Activator
																		.getDefault()
																		.getBundle()
																		.getSymbolicName(),
																SEND_ERRORCODE,
																"Error in sendMessage",
																e));
									}
								}

								public void startTyping(ID userID) {
									try {
										if (typingMessageSender != null)
											typingMessageSender
													.sendTypingMessage(userID,
															true, "");
									} catch (ECFException e) {
										Activator
												.getDefault()
												.getLog()
												.log(
														new Status(
																IStatus.ERROR,
																Activator
																		.getDefault()
																		.getBundle()
																		.getSymbolicName(),
																SEND_ERRORCODE,
																"Error in startTyping",
																e));
									}
								}

								public void disconnect() {
									container.disconnect();
								}

								public void updatePresence(ID userID,
										IPresence presence) {
									try {
										if (presenceSender != null)
											presenceSender.sendPresenceUpdate(
													userID, presence);
									} catch (ECFException e) {
										Activator
												.getDefault()
												.getLog()
												.log(
														new Status(
																IStatus.ERROR,
																Activator
																		.getDefault()
																		.getBundle()
																		.getSymbolicName(),
																SEND_ERRORCODE,
																"Error in sendPresenceUpdate",
																e));
									}
								}

								public void sendRosterAdd(String user,
										String name, String[] groups) {
									// Send roster add
									try {
										rosterSubscriptionSender.sendRosterAdd(
												user, name, groups);
									} catch (ECFException e) {
										Activator
												.getDefault()
												.getLog()
												.log(
														new Status(
																IStatus.ERROR,
																Activator
																		.getDefault()
																		.getBundle()
																		.getSymbolicName(),
																SEND_ERRORCODE,
																"Error in sendRosterAdd",
																e));
									}
								}

								public void sendRosterRemove(ID userID) {
									try {
										if (rosterSubscriptionSender != null)
											rosterSubscriptionSender
													.sendRosterRemove(userID);
									} catch (ECFException e) {
										Activator
												.getDefault()
												.getLog()
												.log(
														new Status(
																IStatus.ERROR,
																Activator
																		.getDefault()
																		.getBundle()
																		.getSymbolicName(),
																SEND_ERRORCODE,
																"Error in sendRosterRemove",
																e));
									}
								}
							};
							PresenceUI.this.groupID = joinedContainer;
							rosterView.addAccount(joinedContainer,
									PresenceUI.this.localUser,
									handler, container, presenceAdapter,
									soContainer);
						}
					});

				} else if (event instanceof IContainerDisconnectedEvent) {
					final IContainerDisconnectedEvent de = (IContainerDisconnectedEvent) event;
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							rosterView.accountDisconnected(de.getTargetID());
						}
					});
				}
			}
		});

		presenceAdapter.getRosterManager().addPresenceListener(
				new IPresenceListener() {

					public void handleRosterEntryAdd(final IRosterEntry entry) {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								rosterView
										.handleRosterEntryAdd(
												PresenceUI.this.groupID,
												entry);
							}
						});
					}

					public void handlePresence(final ID fromID,
							final IPresence presence) {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								rosterView.handlePresence(
										PresenceUI.this.groupID,
										fromID, presence);
							}
						});
					}
					
					public void handleRosterEntryRemove(final IRosterEntry entry) {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								rosterView
										.handleRosterEntryRemove(
												PresenceUI.this.groupID,
												entry);
							}
						});
					}

				});

		presenceAdapter.getRosterManager().addRosterSubscriptionListener(
				new IRosterSubscriptionListener() {

					public void handleSubscribeRequest(final ID fromID) {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								try {
									IWorkbenchWindow ww = PlatformUI
											.getWorkbench()
											.getActiveWorkbenchWindow();
									ReceiveAuthorizeRequestDialog authRequest = new ReceiveAuthorizeRequestDialog(
											ww.getShell(), fromID.getName(),
											localUser.getName());
									authRequest.setBlockOnOpen(true);
									authRequest.open();
									int res = authRequest.getButtonPressed();
									if (res == ReceiveAuthorizeRequestDialog.AUTHORIZE_AND_ADD) {
										if (presenceSender != null) {
											presenceSender
													.sendPresenceUpdate(
															fromID,
															new Presence(
																	IPresence.Type.SUBSCRIBED));
											if (rosterView != null)
												rosterView.sendRosterAdd(
														localUser.getID(),
														fromID.getName(), null);
										}
									} else if (res == ReceiveAuthorizeRequestDialog.AUTHORIZE_ID) {
										if (presenceSender != null) {
											presenceSender
													.sendPresenceUpdate(
															fromID,
															new Presence(
																	IPresence.Type.SUBSCRIBED));
										}
									} else if (res == ReceiveAuthorizeRequestDialog.REFUSE_ID) {
										// do nothing
									} else {
										// do nothing
									}
								} catch (Exception e) {
									Activator
											.getDefault()
											.getLog()
											.log(
													new Status(
															IStatus.ERROR,
															Activator.PLUGIN_ID,
															SEND_ERRORCODE,
															"Exception showing authorization dialog",
															e));
								}
							}
						});
					}

					public void handleSubscribed(ID fromID) {
						// System.out.println("subscribed from "+fromID);
					}

					public void handleUnsubscribed(ID fromID) {
						// System.out.println("unsubscribed from "+fromID);
					}
				});
	}

}
