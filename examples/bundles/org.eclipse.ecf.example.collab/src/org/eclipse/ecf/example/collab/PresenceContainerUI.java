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
package org.eclipse.ecf.example.collab;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainer;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.presence.IAccountManager;
import org.eclipse.ecf.presence.IPresence;
import org.eclipse.ecf.presence.IPresenceContainerAdapter;
import org.eclipse.ecf.presence.IPresenceListener;
import org.eclipse.ecf.presence.IPresenceSender;
import org.eclipse.ecf.presence.IRosterEntry;
import org.eclipse.ecf.presence.IRosterSubscriptionListener;
import org.eclipse.ecf.presence.Presence;
import org.eclipse.ecf.presence.im.ChatMessage;
import org.eclipse.ecf.presence.im.IChatManager;
import org.eclipse.ecf.presence.im.IChatMessage;
import org.eclipse.ecf.presence.im.IChatMessageEvent;
import org.eclipse.ecf.presence.im.IChatMessageListener;
import org.eclipse.ecf.presence.im.IChatMessageSender;
import org.eclipse.ecf.presence.roster.IRosterSubscriptionSender;
import org.eclipse.ecf.presence.ui.MultiRosterView;
import org.eclipse.ecf.ui.dialogs.ReceiveAuthorizeRequestDialog;
import org.eclipse.ecf.ui.views.ILocalInputHandler;
import org.eclipse.ecf.ui.views.RosterView;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class PresenceContainerUI {

	protected static final int SEND_ERRORCODE = 2001;

	protected RosterView rosterView = null;

	protected IPresenceSender presenceSender = null;

	protected IAccountManager accountManager = null;

	protected IRosterSubscriptionSender rosterSubscriptionSender = null;
	
	protected IPresenceContainerAdapter pc = null;

	protected ISharedObjectContainer soContainer = null;

	protected org.eclipse.ecf.core.user.User localUser = null;

	protected ID groupID = null;

	protected IContainer container;
	
	protected IChatManager chatManager;

	protected IChatMessageSender chatMessageSender;
	
	public PresenceContainerUI(IPresenceContainerAdapter pc) {
		this.pc = pc;
		this.presenceSender = pc.getRosterManager().getPresenceSender();
		this.rosterSubscriptionSender = pc.getRosterManager().getRosterSubscriptionSender();
		this.accountManager = pc.getAccountManager();
		this.chatManager = pc.getChatManager();
		this.chatMessageSender = this.chatManager.getChatMessageSender();
	}

	protected void setup(final IContainer container, final ID localUser,
			final String nick) {
		this.container = container;
		this.soContainer = (ISharedObjectContainer) this.container
				.getAdapter(ISharedObjectContainer.class);
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				try {
					IWorkbenchWindow ww = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow();
					IWorkbenchPage wp = ww.getActivePage();
					
					if (pc.getAdapter(IPresenceContainerAdapter.class) == null) {
						IViewPart view = wp
								.showView("org.eclipse.ecf.ui.view.rosterview");
						rosterView = (RosterView) view;
						
						String nickname = null;
						if (nick != null) {
							nickname = nick;
						} else {
							String name = localUser.getName();
							nickname = name.substring(0, name.indexOf("@"));
						}
						PresenceContainerUI.this.localUser = new org.eclipse.ecf.core.user.User(
								localUser, nickname);
					} else {
						MultiRosterView rv = (MultiRosterView) wp.showView("org.eclipse.ecf.presence.ui.view1");
						rv.addContainer(container);
					}
					
				} catch (Exception e) {
					ClientPlugin.getDefault().getLog().log(
							new Status(IStatus.ERROR, ClientPlugin.PLUGIN_ID,
									SEND_ERRORCODE,
									"Exception showing presence view", e));
				}
			}
		});

		chatManager.addChatMessageListener(new IChatMessageListener() {

			public void handleChatMessageEvent(
					final IChatMessageEvent chatMessageEvent) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						IChatMessage message = chatMessageEvent.getChatMessage();
						rosterView.handleMessage(
								PresenceContainerUI.this.groupID, chatMessageEvent.getFromID(), null,
								null, message.getSubject(), message.getBody());
					}
				});
			}});
		/*
		pc.addMessageListener(new IMessageListener() {
			public void handleMessage(final ID fromID, final ID toID,
					final Type type, final String subject, final String message) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						rosterView.handleMessage(
								PresenceContainerUI.this.groupID, fromID, toID,
								type, subject, message);
					}
				});
			}
		});
		*/
		pc.addPresenceListener(new IPresenceListener() {

			public void handleConnected(final ID joinedContainer) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						ILocalInputHandler handler = new ILocalInputHandler() {
							public void inputText(ID userID, String text) {
								try {
									chatMessageSender.sendChatMessage(userID, new ChatMessage(text));
								} catch (ECFException e) {
									ClientPlugin.getDefault().getLog().log(
											new Status(IStatus.ERROR,
													ClientPlugin.getDefault()
															.getBundle()
															.getSymbolicName(),
													SEND_ERRORCODE,
													"Error in sendMessage", e));
								}
							}

							public void startTyping(ID userID) {
								// System.out.println("handleStartTyping("+userID+")");
							}

							public void disconnect() {
								container.disconnect();
								PresenceContainerUI.this.groupID = null;
							}

							public void updatePresence(ID userID,
									IPresence presence) {
								try {
									presenceSender.sendPresenceUpdate(
											userID, presence);
								} catch (ECFException e) {
									ClientPlugin
											.getDefault()
											.getLog()
											.log(
													new Status(
															IStatus.ERROR,
															ClientPlugin
																	.getDefault()
																	.getBundle()
																	.getSymbolicName(),
															SEND_ERRORCODE,
															"Error in sendPresenceUpdate",
															e));
								}
							}

							public void sendRosterAdd(String user, String name,
									String[] groups) {
								// Send roster add
								try {
									rosterSubscriptionSender.sendRosterAdd(user, name, groups);
								} catch (ECFException e) {
									ClientPlugin
											.getDefault()
											.getLog()
											.log(
													new Status(
															IStatus.ERROR,
															ClientPlugin
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
									rosterSubscriptionSender.sendRosterRemove(userID);
								} catch (ECFException e) {
									ClientPlugin
											.getDefault()
											.getLog()
											.log(
													new Status(
															IStatus.ERROR,
															ClientPlugin
																	.getDefault()
																	.getBundle()
																	.getSymbolicName(),
															SEND_ERRORCODE,
															"Error in sendRosterRemove",
															e));
								}
							}
						};
						PresenceContainerUI.this.groupID = joinedContainer;
						rosterView.addAccount(joinedContainer,
								PresenceContainerUI.this.localUser, handler,
								pc, soContainer);
					}
				});
			}

			public void handleRosterEntryAdd(final IRosterEntry entry) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						rosterView.handleRosterEntryAdd(
								PresenceContainerUI.this.groupID, entry);
					}
				});
			}

			public void handlePresence(final ID fromID, final IPresence presence) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						rosterView.handlePresence(
								PresenceContainerUI.this.groupID, fromID,
								presence);
					}
				});
			}

			public void handleDisconnected(final ID departedContainer) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						if (rosterView != null) {
							rosterView.accountDeparted(departedContainer);
						}
					}
				});
				rosterView = null;
			}

			public void handleRosterEntryUpdate(final IRosterEntry entry) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						rosterView.handleRosterEntryAdd(
								PresenceContainerUI.this.groupID, entry);
					}
				});
			}

			public void handleRosterEntryRemove(final IRosterEntry entry) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						rosterView.handleRosterEntryRemove(
								PresenceContainerUI.this.groupID, entry);
					}
				});
			}

		});
		pc.addRosterSubscriptionListener(new IRosterSubscriptionListener() {

			public void handleSubscribeRequest(final ID fromID) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						try {
							IWorkbenchWindow ww = PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow();
							ReceiveAuthorizeRequestDialog authRequest = new ReceiveAuthorizeRequestDialog(
									ww.getShell(), fromID.getName(), localUser
											.getName());
							authRequest.setBlockOnOpen(true);
							authRequest.open();
							int res = authRequest.getButtonPressed();
							if (res == ReceiveAuthorizeRequestDialog.AUTHORIZE_AND_ADD) {
								if (presenceSender != null) {
									presenceSender.sendPresenceUpdate(
											fromID, new Presence(
													IPresence.Type.SUBSCRIBED));
									if (rosterView != null)
										rosterView.sendRosterAdd(localUser,
												fromID.getName(), null);
								}
							} else if (res == ReceiveAuthorizeRequestDialog.AUTHORIZE_ID) {
								if (presenceSender != null) {
									presenceSender.sendPresenceUpdate(
											fromID, new Presence(
													IPresence.Type.SUBSCRIBED));
								}
							} else if (res == ReceiveAuthorizeRequestDialog.REFUSE_ID) {
								// do nothing
							} else {
								// do nothing
							}
						} catch (Exception e) {
							ClientPlugin
									.getDefault()
									.getLog()
									.log(
											new Status(
													IStatus.ERROR,
													ClientPlugin.PLUGIN_ID,
													SEND_ERRORCODE,
													"Exception showing authorization dialog",
													e));
						}
					}
				});
			}

			public void handleUnsubscribeRequest(ID fromID) {
				if (presenceSender != null) {
					try {
						presenceSender.sendPresenceUpdate(fromID, new Presence(IPresence.Type.UNSUBSCRIBED));
					} catch (ECFException e) {
						ClientPlugin.getDefault().getLog().log(
								new Status(IStatus.ERROR, ClientPlugin
										.getDefault().getBundle()
										.getSymbolicName(), SEND_ERRORCODE,
										"Error in sendPresenceUpdate", e));
					}
				}
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
