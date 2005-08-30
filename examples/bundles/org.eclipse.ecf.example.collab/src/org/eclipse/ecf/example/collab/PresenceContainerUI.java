package org.eclipse.ecf.example.collab;

import java.util.Arrays;
import java.util.List;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.presence.IAccountManager;
import org.eclipse.ecf.presence.IInvitationListener;
import org.eclipse.ecf.presence.IMessageListener;
import org.eclipse.ecf.presence.IMessageSender;
import org.eclipse.ecf.presence.IParticipantListener;
import org.eclipse.ecf.presence.IPresence;
import org.eclipse.ecf.presence.IPresenceContainer;
import org.eclipse.ecf.presence.IPresenceListener;
import org.eclipse.ecf.presence.IPresenceSender;
import org.eclipse.ecf.presence.IRosterEntry;
import org.eclipse.ecf.presence.ISubscribeListener;
import org.eclipse.ecf.presence.chat.IChatRoomContainer;
import org.eclipse.ecf.presence.chat.IChatRoomManager;
import org.eclipse.ecf.presence.chat.IRoomInfo;
import org.eclipse.ecf.presence.impl.Presence;
import org.eclipse.ecf.ui.dialogs.AddBuddyDialog;
import org.eclipse.ecf.ui.dialogs.ReceiveAuthorizeRequestDialog;
import org.eclipse.ecf.ui.views.ILocalInputHandler;
import org.eclipse.ecf.ui.views.RosterView;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class PresenceContainerUI {
	
    protected RosterView rosterView = null;
    protected IMessageSender messageSender = null;
    protected IPresenceSender presenceSender = null;
	protected IAccountManager accountManager = null;
	protected IPresenceContainer pc = null;
	
	protected org.eclipse.ecf.core.user.User localUser = null;
	protected ID groupID = null;
	protected IContainer container;
	
	public PresenceContainerUI(IPresenceContainer pc) {
		this.pc = pc;
        this.messageSender = pc.getMessageSender();
        this.presenceSender = pc.getPresenceSender();
		this.accountManager = pc.getAccountManager();		
	}
	
    protected void setup(final IContainer container, final ID localUser, final String nick) {
    	this.container = container;
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                try {
                    IWorkbenchWindow ww = PlatformUI.getWorkbench()
                            .getActiveWorkbenchWindow();
                    IWorkbenchPage wp = ww.getActivePage();
                    IViewPart view = wp.showView("org.eclipse.ecf.ui.view.rosterview");
                    rosterView = (RosterView) view;
                    String nickname = null;
                    if (nick != null) {
                        nickname = nick;
                    } else {
                        String name = localUser.getName();
                        nickname = name.substring(0,name.indexOf("@"));
                    }
                    PresenceContainerUI.this.localUser = new org.eclipse.ecf.core.user.User(localUser,nickname);
                } catch (Exception e) {
                    IStatus status = new Status(IStatus.ERROR,ClientPlugin.PLUGIN_ID,IStatus.OK,"Exception showing presence view",e);
                    ClientPlugin.getDefault().getLog().log(status);
                }
            }
        });

        pc.addMessageListener(new IMessageListener() {
            public void handleMessage(final ID fromID, final ID toID, final Type type, final String subject, final String message) {
                Display.getDefault().syncExec(new Runnable() {
                    public void run() {
                        rosterView.handleMessage(PresenceContainerUI.this.groupID,fromID,toID,type,subject,message);
                    }
                });
            }                
        });
        pc.addPresenceListener(new IPresenceListener() {

            public void handleContainerJoined(final ID joinedContainer) {
                Display.getDefault().syncExec(new Runnable() {
                    public void run() {
                        ILocalInputHandler handler = new ILocalInputHandler() {
                            public void inputText(ID userID, String text) {
                                messageSender.sendMessage(localUser,userID,null,null,text);
                            }
                            public void startTyping(ID userID) {
                                //System.out.println("handleStartTyping("+userID+")");
                            }
                            public void disconnect() {
                                container.disconnect();
                                PresenceContainerUI.this.groupID = null;
                            }
    						public void updatePresence(ID userID, IPresence presence) {
    							presenceSender.sendPresenceUpdate(localUser,userID,presence);
    						}
    						public void sendRosterAdd(String user, String name, String[] groups) {
    							// Send roster add
    							presenceSender.sendRosterAdd(localUser, user,name,groups);
    						}
    						public void sendRosterRemove(ID userID) {
    							presenceSender.sendRosterRemove(localUser, userID);
    						}
                        };
                        PresenceContainerUI.this.groupID = joinedContainer;
                        rosterView.addAccount(joinedContainer,PresenceContainerUI.this.localUser,handler);
                    }
                });
            }

            public void handleRosterEntry(final IRosterEntry entry) {
                Display.getDefault().syncExec(new Runnable() {
                    public void run() {
                        rosterView.handleRosterEntry(PresenceContainerUI.this.groupID,entry);
                    }
                });
            }

            public void handlePresence(final ID fromID, final IPresence presence) {
                Display.getDefault().syncExec(new Runnable() {
                    public void run() {
                        rosterView.handlePresence(PresenceContainerUI.this.groupID,fromID,presence);
                    }
                });
            }

            public void handleContainerDeparted(final ID departedContainer) {
                Display.getDefault().syncExec(new Runnable() {
                    public void run() {
						if (rosterView != null) {
							rosterView.accountDeparted(departedContainer);
						}
                    }
                });
                messageSender = null;
                rosterView = null;
            }

			public void handleSetRosterEntry(final IRosterEntry entry) {
                Display.getDefault().syncExec(new Runnable() {
                    public void run() {
                        rosterView.handleSetRosterEntry(PresenceContainerUI.this.groupID,entry);
                    }
                });
			}
            
        });
		pc.addSubscribeListener(new ISubscribeListener() {

			public void handleSubscribeRequest(final ID fromID, IPresence presence) {
		        Display.getDefault().syncExec(new Runnable() {
		            public void run() {
		                try {
		                    IWorkbenchWindow ww = PlatformUI.getWorkbench()
		                            .getActiveWorkbenchWindow();
							ReceiveAuthorizeRequestDialog authRequest = new ReceiveAuthorizeRequestDialog(ww.getShell(),fromID.getName(),localUser.getName());
							authRequest.setBlockOnOpen(true);
							authRequest.open();
							int res = authRequest.getButtonPressed();
							if (res == ReceiveAuthorizeRequestDialog.AUTHORIZE_AND_ADD) {								
								if (presenceSender != null) {
									presenceSender.sendPresenceUpdate(localUser,fromID,new Presence(IPresence.Type.SUBSCRIBED));
									// Get group info here
									if (rosterView != null) {
										String [] groupNames = rosterView.getGroupNames();
										List g = Arrays.asList(groupNames);
										String selectedGroup = rosterView.getSelectedGroupName();
										int selected = (selectedGroup==null)?-1:g.indexOf(selectedGroup);
										AddBuddyDialog sg = new AddBuddyDialog(ww.getShell(),fromID.getName(),groupNames,selected);
										sg.open();
										if (sg.getResult() == Window.OK) {
											String group = sg.getGroup();
											String user = sg.getUser();
											String nickname = sg.getNickname();
											sg.close();
											if (!g.contains(group)) {
												// create group with name
												rosterView.addGroup(groupID,group);
											}
											// Finally, send the information and request subscription
											presenceSender.sendRosterAdd(localUser, user,nickname,new String[] { group } );
										}
									}
								} 
							} else if (res == ReceiveAuthorizeRequestDialog.AUTHORIZE_ID) {
								if (presenceSender != null) {
									presenceSender.sendPresenceUpdate(localUser,fromID,new Presence(IPresence.Type.SUBSCRIBED));
								} 
							} else if (res == ReceiveAuthorizeRequestDialog.REFUSE_ID) {
								System.out.println("Refuse hit");
							} else {
								System.out.println("No buttons hit");
							}
						} catch (Exception e) {
		                    IStatus status = new Status(IStatus.ERROR,ClientPlugin.PLUGIN_ID,IStatus.OK,"Exception showing authorization dialog",e);
		                    ClientPlugin.getDefault().getLog().log(status);
						}
		            }
		        });
			}

			public void handleUnsubscribeRequest(ID fromID, IPresence presence) {
				if (presenceSender != null) {
					presenceSender.sendPresenceUpdate(localUser,fromID,new Presence(IPresence.Type.UNSUBSCRIBED));
				}
			}

			public void handleSubscribed(ID fromID, IPresence presence) {
				//System.out.println("subscribed from "+fromID);			
			}

			public void handleUnsubscribed(ID fromID, IPresence presence) {
				//System.out.println("unsubscribed from "+fromID);			
			}
		});
    }

    protected void connectToFirstChatRoom() throws ECFException {
    	// Get chat room manager for presence container
		IChatRoomManager crmanager = pc.getChatRoomManager();
		// Get info from manager for chat rooms
		IRoomInfo [] roomInfos = crmanager.getChatRoomsInfo();
		// If no chat rooms available we throw
		if (roomInfos == null || roomInfos.length == 0) {
			throw new ContainerConnectException("No chat rooms available for "+container.getConnectedID().getName());
		}
		// Otherwise print out some info about rooms
		System.out.println(" chat rooms available: "+Arrays.toString(roomInfos));
		
		// XXX arbitrarily join first chat room
		ID targetID = roomInfos[0].getRoomID();
		
		// Make chat room container
		IChatRoomContainer chatRoom = crmanager.makeChatRoomContainer();
		// Add listeners/UI handlers for appropriate events
		chatRoom.addMessageListener(new IMessageListener() {
			public void handleMessage(ID fromID, ID toID, Type type, String subject, String messageBody) {
				System.out.println("Room message from="+fromID+",to="+toID+",type="+type+",sub="+subject+",body="+messageBody);
			}
		});
		chatRoom.addParticipantListener(new IParticipantListener() {
			public void handlePresence(ID fromID, IPresence presence) {
				System.out.println("chat presence from="+fromID+",presence="+presence);
			}});
		chatRoom.addInvitationListener(new IInvitationListener() {
			public void handleInvitationReceived(ID roomID, ID from, ID toID, String subject, String body) {
				System.out.println("invitation room="+roomID+",from="+from+",to="+toID+",subject="+subject+",body="+body);
			}});
		
		// Now connect to room ID
		chatRoom.connect(targetID,null);
	}
}
