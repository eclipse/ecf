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

package org.eclipse.ecf.ui.views;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.user.IUser;
import org.eclipse.ecf.core.user.User;
import org.eclipse.ecf.presence.IMessageListener;
import org.eclipse.ecf.presence.IPresence;
import org.eclipse.ecf.presence.IPresenceListener;
import org.eclipse.ecf.presence.IRosterEntry;
import org.eclipse.ecf.presence.IRosterGroup;
import org.eclipse.ecf.presence.impl.RosterEntry;
import org.eclipse.ecf.ui.UiPlugin;
import org.eclipse.ecf.ui.UiPluginConstants;
import org.eclipse.ecf.ui.dialogs.AddBuddyDialog;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class RosterView extends ViewPart implements IPresenceListener, IMessageListener {
    public static final String DISCONNECT_ICON_DISABLED = "icons/disabled/terminate_co.gif";
    public static final String DISCONNECT_ICON_ENABLED = "icons/enabled/terminate_co.gif";

    public static final String INSTANT_MESSAGE_ICON = "icons/enabled/message.gif";
    public static final String ADDGROUP_ICON = "icons/enabled/addgroup.gif";

    protected static final int TREE_EXPANSION_LEVELS = 2;
    private TreeViewer viewer;
    private Action chatAction;
    private Action selectedChatAction;
    private Action selectedDoubleClickAction;
    private Action disconnectAction;
	private Action addGroupAction;
	
    protected IUser localUser;
    protected ILocalInputHandler inputHandler;
    protected Hashtable chatThreads = new Hashtable();
    protected ID groupID;
    
    protected IUser getLocalUser() {
        return localUser;
    }
	
	protected String getUserNameFromID(ID userID) {
		if (userID == null) return "";
		String uname = userID.getName();
		int index = uname.indexOf("@");
		String username = uname;
		if (index >= 0) {
			username = uname.substring(0,index);
		}
		if (username.equals("")) {
			return uname;
		} else return username;
	}
	public void dispose() {
		if (inputHandler != null) {
			inputHandler.disconnect();
			inputHandler = null;
		}
		super.dispose();
	}
    class TreeObject implements IAdaptable {
        private String name;
        private TreeParent parent;
        private ID userID;

        public TreeObject(String name, ID userID) {
            this.name = name;
            this.userID = userID;
        }

        public TreeObject(String name) {
            this(name, null);
        }

        public String getName() {
            return name;
        }

        public ID getUserID() {
            return userID;
        }

        public void setParent(TreeParent parent) {
            this.parent = parent;
        }

        public TreeParent getParent() {
            return parent;
        }

        public String toString() {
            return getName();
        }

        public Object getAdapter(Class key) {
            return null;
        }
    }

    class TreeParent extends TreeObject {
        private ArrayList children;

        public TreeParent(String name) {
            super(name);
            children = new ArrayList();
        }

        public TreeParent(String name, ID userID) {
            super(name, userID);
            children = new ArrayList();
        }
        public void addChild(TreeObject child) {
            children.add(child);
            child.setParent(this);
        }

        public void removeChild(TreeObject child) {
            children.remove(child);
            child.setParent(null);
        }

        public void removeChildren() {
            for (Iterator i = children.iterator(); i.hasNext();) {
                TreeObject obj = (TreeObject) i.next();
                obj.setParent(null);
            }
            children.clear();
        }

        public TreeObject[] getChildren() {
            return (TreeObject[]) children.toArray(new TreeObject[children
                    .size()]);
        }

        public boolean hasChildren() {
            return children.size() > 0;
        }
		
    }
	
	class TreeGroup extends TreeParent {
		
		public TreeGroup(String name) {
			super(name);
		}
		public int getActiveCount() {
			TreeObject [] childs = getChildren();
			int totCount = 0;
			for(int i=0; i < childs.length; i++) {
				if (childs[i] instanceof TreeBuddy) {
					TreeBuddy tb = (TreeBuddy) childs[i];
					if (tb.isActive()) {
						totCount++;
					}
				}
			}
			return totCount;
		}
		public int getTotalCount() {
			return getChildren().length;
		}
	}
	
	class TreeBuddy extends TreeParent {
		IPresence presence = null;
		public TreeBuddy(String name, ID id, IPresence p) {
			super(name,id);
			this.presence = p;
		}
		public IPresence getPresence() {
			return presence;
		}
		public void setPresence(IPresence p) {
			this.presence = p;
		}
		public boolean isActive() {
			IPresence p = getPresence();
			if (p == null) return false;
			return presence.getType().equals(IPresence.Type.AVAILABLE);
		}
	}

    class ViewContentProvider implements IStructuredContentProvider,
            ITreeContentProvider {
        private TreeParent invisibleRoot;
        private TreeParent root;

        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }

        public void dispose() {
        }

        public Object[] getElements(Object parent) {
            if (parent.equals(getViewSite())) {
                if (root == null)
                    initialize();
                return getChildren(root);
            }
            return getChildren(parent);
        }

        public Object getParent(Object child) {
            if (child instanceof TreeObject) {
                return ((TreeObject) child).getParent();
            }
            return null;
        }

        public Object[] getChildren(Object parent) {
            if (parent instanceof TreeParent) {
                return ((TreeParent) parent).getChildren();
            }
            return new Object[0];
        }

        public boolean hasChildren(Object parent) {
            if (parent instanceof TreeParent)
                return ((TreeParent) parent).hasChildren();
            return false;
        }

		public TreeBuddy fillPresence(TreeBuddy obj, IPresence presence) {
			obj.setPresence(presence);
			obj.removeChildren();
			obj.addChild(new TreeObject("Account: "+obj.getUserID().getName()));
            TreeObject type = new TreeObject("Status: "
                    + presence.getType().toString());
			obj.addChild(type);
            String status = presence.getStatus();
            if (status != null && !status.equals("")) {
                TreeObject stat = new TreeObject("Details: " + status);
				obj.addChild(stat);
            }
            Map props = presence.getProperties();
            for (Iterator i = props.keySet().iterator(); i.hasNext();) {
                String key = (String) i.next();
                String value = (String) props.get(key);
                if (key != null && value != null) {
                    TreeObject prop = new TreeObject(key + ": " + value);
					obj.addChild(prop);
                }
            }
            return obj;
        }

        public TreeBuddy createBuddy(TreeBuddy oldBuddy, IRosterEntry entry) {
			String name = entry.getName();
			if (name == null) name = getUserNameFromID(entry.getUserID());
            IPresence presence = entry.getPresenceState();
			TreeBuddy newBuddy = null;
			if (oldBuddy==null) newBuddy = new TreeBuddy(name,entry.getUserID(),presence);
			else newBuddy = oldBuddy;
			if (presence != null) fillPresence(newBuddy, presence);
			else if (oldBuddy == null) newBuddy.addChild(new TreeObject("Account: "+newBuddy.getUserID().getName()));				
            return newBuddy;
        }

		public TreeGroup findGroup(TreeParent parent, String name) {
            TreeObject [] objs = parent.getChildren();
            if (objs != null) {
                for(int i = 0; i < objs.length; i++) {
                    if (objs[i].getName().equals(name)) {
                        return (TreeGroup) objs[i];
                    }
                }
            }
            return null;
        }
		public String[] getAllGroupNames() {
			TreeObject [] objs = root.getChildren();
			String [] groups = null;
			if (objs != null) {
				List l = new ArrayList();
				for(int i=0; i < objs.length; i++) {
					TreeObject o = objs[i];
					if (o instanceof TreeGroup) {
						l.add(((TreeGroup)o).getName());
					}
				}
				return (String []) l.toArray(new String[] {});
			} else return new String[0];
		}
		public TreeBuddy findBuddy(TreeParent parent, IRosterEntry entry) {
			return findBuddy(parent,entry.getUserID());
		}

		public TreeBuddy findBuddy(TreeParent parent, ID entryID) {
			TreeObject [] objs = parent.getChildren();
			if (objs == null) return null;
			for(int i=0; i < objs.length; i++) {
				if (objs[i] instanceof TreeBuddy) {
					TreeBuddy tb = (TreeBuddy) objs[i];
					ID tbid = tb.getUserID();
					if (tbid != null && tbid.equals(entryID)) {
						return (TreeBuddy) objs[i];
					}
				} else if (objs[i] instanceof TreeGroup) {
					TreeBuddy found = findBuddy((TreeParent) objs[i],entryID);
					if (found != null) return found;
				}
			}
			return null;
		}

        public TreeBuddy findAndReplaceEntry(TreeParent parent, IRosterEntry entry) {
			TreeBuddy tb = findBuddy(parent,entry);
			TreeBuddy result = createBuddy(tb,entry);
			// If buddy found already, then remove old and add new
			if (tb != null) {
				TreeParent tbparent = tb.getParent();
				tbparent.removeChild(tb);
				tbparent.addChild(result);
			}
			return result;
        }
        public void addEntry(TreeParent parent, IRosterEntry entry) {
			
			TreeBuddy newBuddy = findAndReplaceEntry(parent,entry);
			TreeParent buddyParent = newBuddy.getParent();
			
			if (buddyParent == null) {
                // Existing group not found, so see if entry has a group associated with it
                Iterator groups = entry.getGroups();
                if (groups.hasNext()) {
                    // There's a group associated with entry...so add with group name
                    String groupName = ((IRosterGroup) groups.next()).getName();
					TreeGroup oldgrp = findGroup(parent,groupName);
					if (oldgrp != null) {
						oldgrp.addChild(newBuddy);
					} else {
						TreeGroup newgrp = new TreeGroup(groupName);
						newgrp.addChild(newBuddy);
						parent.addChild(newgrp);
					}
                } else {
					TreeGroup tg = new TreeGroup("Unfiled");
					tg.addChild(newBuddy);
					parent.addChild(tg);
                }
			}
		}
		
		public void addGroup(String name) {
			if (name == null) return;
			addGroup(root,name);
		}
		public void addGroup(TreeParent parent, String name) {
			TreeGroup oldgrp = findGroup(parent,name);
			if (oldgrp != null) {
				// If the name is already there, then skip
				return;
			}
			// Group not there...add it
			TreeGroup newgrp = new TreeGroup(name);
			parent.addChild(newgrp);
		}
		public void removeGroup(TreeParent parent, String name) {
			TreeGroup oldgrp = findGroup(parent,name);
			if (oldgrp == null) {
				// if not there, simply return
				return;
			}
			// Else it is there...and we remove it
			parent.removeChild(oldgrp);
		}
		public void removeGroup(String name) {
			if (name == null) return;
			removeGroup(root,name);
		}
        public void addEntry(IRosterEntry entry) {
            addEntry(root, entry);
        }
		public void removeRosterEntry(ID entry) {
			removeEntry(root,entry);
		}
		public void removeEntry(TreeParent parent, ID entry) {
			TreeBuddy buddy = findBuddy(parent,entry);
			if (buddy == null) return;
			TreeParent p = buddy.getParent();
			if (p != null) {
				p.removeChild(buddy);
				refreshView();
			}
		}
        public void removeAllEntries() {
            root = null;
        }
        private void initialize() {
            root = new TreeParent("Buddy List");
            invisibleRoot = new TreeParent("");
            invisibleRoot.addChild(root);
        }
    }

    class ViewLabelProvider extends LabelProvider {
        public String getText(Object obj) {
			String label = null;
			if (obj instanceof TreeGroup) {
				TreeGroup tg = (TreeGroup) obj;
				label = tg.getName() + " ("+tg.getActiveCount()+"/"+tg.getTotalCount()+")";
				return label;
			} else return obj.toString();
        }

        public Image getImage(Object obj) {
            Image image = null;     //By default, no image exists for obj, but if found to be a specific instance, load from plugin repository.
            if (obj instanceof TreeBuddy) {
	            ImageRegistry registry = UiPlugin.getDefault().getImageRegistry();	            
                TreeBuddy o = (TreeBuddy) obj;
                if (o.getUserID() != null) {
					if (o.isActive()) {
						image = registry.get(UiPluginConstants.DECORATION_USER);
					} else {
						image = registry.get(UiPluginConstants.DECORATION_USER_INACTIVE);
					}
                }
            } else if (obj instanceof TreeGroup) {
				image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
            }
            return image;
        }
    }

    class NameSorter extends ViewerSorter {
    }

    public RosterView() {
    }

    protected void refreshView() {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                try {
                    viewer.refresh();
                    expandAll();
                } catch (Exception e) {
                }
            }
        });
    }

    protected void expandAll() {
        viewer.expandToLevel(TREE_EXPANSION_LEVELS);
    }

    public void createPartControl(Composite parent) {
        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.setContentProvider(new ViewContentProvider());
        viewer.setLabelProvider(new ViewLabelProvider());
        viewer.setSorter(new NameSorter());
        viewer.setInput(getViewSite());
        viewer.setAutoExpandLevel(3);
        makeActions();
        hookContextMenu();
        hookDoubleClickAction();
        contributeToActionBars();
    }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                RosterView.this.fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalPullDown(IMenuManager manager) {
        manager.add(addGroupAction);
        manager.add(new Separator());
        manager.add(chatAction);
        manager.add(new Separator());
        manager.add(disconnectAction);
    }

    private void fillContextMenu(IMenuManager manager) {
        final TreeObject treeObject = getSelectedTreeObject();
        final ID targetID = treeObject.getUserID();
        if (treeObject != null) {
			if (treeObject instanceof TreeBuddy) {
				final TreeBuddy tb = (TreeBuddy) treeObject;
	            selectedChatAction = new Action() {
	                public void run() {
	                    openChatWindowForTarget(targetID);
	                }
	            };
	            selectedChatAction.setText("Send IM to "+treeObject.getUserID().getName());
	            selectedChatAction.setImageDescriptor(ImageDescriptor.createFromURL(
		                UiPlugin.getDefault().find(new Path(INSTANT_MESSAGE_ICON))));
	            manager.add(selectedChatAction);
				
				
				TreeObject parent = treeObject.getParent();
				TreeGroup tg = null;
				if (parent != null && parent instanceof TreeGroup) {
					tg = (TreeGroup) parent;
				}
				final TreeGroup treeGroup = tg;
				Action removeUserAction = new Action() {
					public void run() {
						removeUserFromGroup(tb,treeGroup);
					}
				};
				if (treeGroup != null) {
					removeUserAction.setText("Remove "+treeObject.getName()+" from "+treeGroup.getName());
				} else {
					removeUserAction.setText("Remove "+treeObject.getName());
				}
				manager.add(removeUserAction);
				
			} else if (treeObject instanceof TreeGroup) {
				final TreeGroup treeGroup = (TreeGroup) treeObject;
				final String groupName = treeGroup.getName();
				Action addUserAction = new Action() {
					public void run() {
						addUserToGroup(groupName);
					}
				};
				addUserAction.setText("Add buddy to "+treeObject.getName());
				addUserAction.setImageDescriptor(ImageDescriptor.createFromURL(
                UiPlugin.getDefault().find(new Path(ADDGROUP_ICON))));
				manager.add(addUserAction);
			}
        }
        
        manager.add(new Separator());
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

	protected void addUserToGroup(String groupName) {
		String [] groupNames = getGroupNames();
		String selected = getSelectedGroupName();
		int index = -1;
		if (selected != null && selected.equals(groupName)) {
			index = 0;
		}
		AddBuddyDialog sg = new AddBuddyDialog(viewer.getControl().getShell(),null,new String[] { groupName },index);
		sg.open();
		if (sg.getReturnCode() == Window.OK) {
			String group = sg.getGroup();
			String user = sg.getUser();
			String nickname = sg.getNickname();
			sg.close();
			if (!Arrays.asList(groupNames).contains(group)) {
				// create group with name
				addGroup(group);
			}
			inputHandler.sendRosterAdd(user,nickname,new String[] { group } );
		}
	}
	
	protected void removeUserFromGroup(TreeBuddy buddy, TreeGroup group) {
		if (inputHandler != null) {
			inputHandler.sendRosterRemove(buddy.getUserID());
		} 
	}
    protected TreeObject getSelectedTreeObject() {
        ISelection selection = viewer.getSelection();
        Object obj = ((IStructuredSelection) selection)
                .getFirstElement();
        TreeObject treeObject = (TreeObject) obj;
        return treeObject;
    }
    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(addGroupAction);
        manager.add(new Separator());
        manager.add(chatAction);
        manager.add(new Separator());
        manager.add(disconnectAction);
    }

    protected ID inputIMTarget() {
        InputDialog dlg = new InputDialog(getSite().getShell(),"Send IM","Please enter the XMPP ID of the person you would like to IM","",null);
        dlg.setBlockOnOpen(true);
        int res = dlg.open();
        if (res == InputDialog.OK) {
            String strres = dlg.getValue();
            if (strres != null && !strres.equals("")) {
                ID target = null;
                try {
                    target = IDFactory.makeStringID(strres);
                } catch (Exception e) {
                    MessageDialog.openError(getSite().getShell(),"Error","Error in IM target");
                    return null;
                }
                return target;
            }
        }
        return null;
    }
    
    private void makeActions() {
        chatAction = new Action() {
            public void run() {
                ID targetID = inputIMTarget();
                if (targetID != null) openChatWindowForTarget(targetID);
            }
        };
        chatAction.setText("Send Instant Message...");
        chatAction.setToolTipText("Send instant message to arbitrary user");
        chatAction.setImageDescriptor(ImageDescriptor.createFromURL(
		                UiPlugin.getDefault().find(new Path(INSTANT_MESSAGE_ICON))));
        chatAction.setEnabled(false);
        selectedDoubleClickAction = new Action() {
            public void run() {
                TreeObject treeObject = getSelectedTreeObject();
                final ID targetID = treeObject.getUserID();
                if (targetID != null) openChatWindowForTarget(targetID);
            }
        };
        disconnectAction = new Action() {
            public void run() {
                if (inputHandler != null) {
                    inputHandler.disconnect();
                    chatAction.setEnabled(false);
					addGroupAction.setEnabled(false);
					disconnectAction.setEnabled(false);
                    this.setEnabled(false);
                }
            }
        };
        disconnectAction.setText("Disconnect");
        disconnectAction.setToolTipText("Disconnect from server");
        disconnectAction.setEnabled(false);
        disconnectAction.setImageDescriptor(ImageDescriptor.createFromURL(
                UiPlugin.getDefault().find(new Path(DISCONNECT_ICON_ENABLED))));        
        disconnectAction.setDisabledImageDescriptor(ImageDescriptor.createFromURL(
                UiPlugin.getDefault().find(new Path(DISCONNECT_ICON_DISABLED))));     
		
		
		addGroupAction = new Action() {
			public void run() {
				// handle add group operation here
				String defaultNewGroupName = "New Group";
				InputDialog input = new InputDialog(viewer.getControl().getShell(),"Add Group","Please enter the name of the group to be added",defaultNewGroupName,new IInputValidator() {

					public String isValid(String newText) {
						if (newText == null || newText.length() == 0) {
							return "New group name cannot be empty";
						} else {
							String [] groupNames = getGroupNames();
							for(int i=0; i < groupNames.length; i++) {
								if (groupNames[i].equals(newText)) {
									return "A group named '"+newText+"' already exists.  Please choose another name";
								}
							}
						}
						return null;
					}
					
				});
				input.open();
				String result = input.getValue();
				// Now add the group
				addGroup(result);
			}
		};
		addGroupAction.setText("Add Group...");
		addGroupAction.setToolTipText("Add group");
		addGroupAction.setEnabled(false);
		addGroupAction.setImageDescriptor(ImageDescriptor.createFromURL(
                UiPlugin.getDefault().find(new Path(ADDGROUP_ICON))));
    }


    protected ChatWindow openChatWindowForTarget(ID targetID) {
        if (targetID == null)
            return null;
        ChatWindow window = null;
        synchronized (chatThreads) {
            window = (ChatWindow) chatThreads.get(targetID);
            if (window == null) {
                window = makeChatWindowForTarget(targetID);
                window.open();
            } else {
                if (!window.hasFocus()) {
                    window.openAndFlash();
                }
            }
            window.setStatus("chat with "+targetID.getName());
        }
        return window;
    }

    protected ChatWindow makeChatWindowForTarget(ID targetID) {
        ChatWindow window = new ChatWindow(RosterView.this, targetID.getName(),
                getWindowInitText(targetID), getLocalUser(), new User(targetID));
        window.create();
        chatThreads.put(targetID, window);
        return window;
    }

    private void hookDoubleClickAction() {
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                selectedDoubleClickAction.run();
            }
        });
    }

    private void showMessage(String message) {
        MessageDialog.openInformation(viewer.getControl().getShell(),
                "Roster View", message);
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    public void handleRosterEntry(IRosterEntry entry) {
        if (entry == null)
            return;
        ViewContentProvider vcp = (ViewContentProvider) viewer
                .getContentProvider();
        if (vcp != null) {
			if (entry.getInterestType() == IRosterEntry.InterestType.REMOVE ||
					entry.getInterestType() == IRosterEntry.InterestType.NONE) {
				vcp.removeRosterEntry(entry.getUserID());
			} else vcp.addEntry(entry);
            refreshView();
        }
    }

    public void handlePresence(ID userID, IPresence presence) {
        IRosterEntry entry = new RosterEntry(userID, null, presence);
        handleRosterEntry(entry);
        refreshView();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.ui.views.IConfigViewer#setUser(org.eclipse.ecf.core.user.IUser)
     */
    public void setLocalUser(IUser user, ILocalInputHandler textInputHandler) {
        this.localUser = user;
        this.inputHandler = textInputHandler;
    }

    public Object getAdapter(Class clazz) {
        if (clazz != null && clazz.equals(ILocalInputHandler.class)) {
            return new ILocalInputHandler() {
                public void inputText(ID userID, String text) {
                    if (inputHandler != null) {
                        inputHandler.inputText(userID, text);
                    } else
                        System.out.println("handleTextLine(" + text + ")");
                }

                public void startTyping(ID userID) {
                    if (inputHandler != null) {
                        inputHandler.startTyping(userID);
                    } else
                        System.out.println("handleStartTyping()");
                }
                public void disconnect() {
                    if (inputHandler != null) {
                        inputHandler.disconnect();
                    } else
                        System.out.println("disconnect()");
                }

				public void updatePresence(ID userID, IPresence presence) {
                    if (inputHandler != null) {
                        inputHandler.updatePresence(userID,presence);
                    } else
                        System.out.println("disconnect()");
				}

				public void sendRosterAdd(String user, String name, String[] groups) {
                    if (inputHandler != null) {
                        inputHandler.sendRosterAdd(user,name,groups);
                    } else
                        System.out.println("sendRosterAdd()");
				}

				public void sendRosterRemove(ID userID) {
                    if (inputHandler != null) {
                        inputHandler.sendRosterRemove(userID);
                    } else
                        System.out.println("sendRosterRemove()");
				}
            };
        } else if (clazz.equals(IPresenceListener.class)) {
            return this;
        } else if (clazz.equals(IMessageListener.class)) {
            return this;
        } else
            return null;
    }

    protected String getWindowInitText(ID targetID) {
        String result = "chat with " + targetID.getName() + " started "
                + getDateAndTime() + "\n\n";
        return result;
    }

    protected String getDateAndTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM:dd hh:mm:ss");
        return sdf.format(new Date());
    }

    public void handleMessage(ID fromID, ID toID, Type type, String subject,
            String message) {
        ChatWindow window = openChatWindowForTarget(fromID);
        // finally, show message
        if (window != null) {
            window.handleMessage(fromID, toID, type, subject, message);
            window.setStatus("last message received at "+(new SimpleDateFormat("hh:mm:ss").format(new Date())));
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.ui.views.IConfigViewer#setGroup(org.eclipse.ecf.core.identity.ID)
     */
    public void setGroup(ID groupManager) {
        if (groupManager != null) {
            groupID = groupManager;
            disconnectAction.setEnabled(true);
            chatAction.setEnabled(true);
			addGroupAction.setEnabled(true);
        }
    }

    public void memberDeparted(ID member) {
        if (groupID != null) {
            if (groupID.equals(member)) {
                handleGroupManagerDeparted();
            }
        }
    }
    public void handleContainerJoined(ID containerID) {
        // do nothing on this notification for now
    }
    public void handleContainerDeparted(ID containerID) {
        handleGroupManagerDeparted();
    }
    protected void disposeAllChatWindows(String status) {
        synchronized (chatThreads) {
            for(Iterator i=chatThreads.values().iterator(); i.hasNext(); ) {
                ChatWindow window = (ChatWindow) i.next();
                window.setDisposed(status);
            }
            chatThreads.clear();
        }
    }
    
    protected void removeAllRosterEntries() {
        ViewContentProvider vcp = (ViewContentProvider) viewer
                .getContentProvider();
        if (vcp != null) {
            vcp.removeAllEntries();
            refreshView();
        }
    }
	
	public String[] getGroupNames() {
        ViewContentProvider vcp = (ViewContentProvider) viewer
        .getContentProvider();
		if (vcp != null) {
			return vcp.getAllGroupNames();
		} else return new String[0];
	}
	public String getSelectedGroupName() {
		TreeObject to = getSelectedTreeObject();
		if (to == null) return null;
		if (to instanceof TreeGroup) {
			TreeGroup tg = (TreeGroup) to;
			return tg.getName();
		}
		return null;
	}
	public void addGroup(String name) {
        ViewContentProvider vcp = (ViewContentProvider) viewer
        .getContentProvider();
		if (vcp != null) {
			vcp.addGroup(name);
			refreshView();
		} 
	}
	public void removeGroup(String name) {
        ViewContentProvider vcp = (ViewContentProvider) viewer
        .getContentProvider();
		if (vcp != null) {
			vcp.removeGroup(name);
			refreshView();
		} 
	}
	public void removeRosterEntry(ID id) {
        ViewContentProvider vcp = (ViewContentProvider) viewer
        .getContentProvider();
		if (vcp != null) {
			vcp.removeRosterEntry(id);
			refreshView();
		} 
	}
    protected void handleGroupManagerDeparted() {
        removeAllRosterEntries();
        disposeAllChatWindows("Disconnected from server.  Chat is inactive");
        chatAction.setEnabled(false);
        disconnectAction.setEnabled(false);
    }

}