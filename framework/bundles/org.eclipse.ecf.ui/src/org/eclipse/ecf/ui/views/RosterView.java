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
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.user.IUser;
import org.eclipse.ecf.core.user.User;
import org.eclipse.ecf.ui.UiPlugin;
import org.eclipse.ecf.ui.UiPluginConstants;
import org.eclipse.ecf.ui.messaging.IMessageViewer;
import org.eclipse.ecf.ui.presence.IPresence;
import org.eclipse.ecf.ui.presence.IRosterEntry;
import org.eclipse.ecf.ui.presence.IRosterGroup;
import org.eclipse.ecf.ui.presence.IRosterViewer;
import org.eclipse.ecf.ui.presence.RosterEntry;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
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

/**
 * WARNING: IN PROGRESS
 * 
 */
public class RosterView extends ViewPart implements IConfigViewer,
        IRosterViewer, IMessageViewer {
    protected static final int TREE_EXPANSION_LEVELS = 1;
    private TreeViewer viewer;
    private Action chatAction;
    private Action selectedChatAction;
    private Action selectedDoubleClickAction;
    private Action disconnectAction;
    protected IUser localUser;
    protected ITextInputHandler textInputHandler;
    protected Hashtable chatThreads = new Hashtable();
    protected ID groupID;
    
    protected IUser getLocalUser() {
        return localUser;
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

        public TreeParent hasGroup(IRosterGroup group) {
            if (group == null)
                return null;
            TreeObject[] children = root.getChildren();
            if (children == null)
                return null;
            for (int i = 0; i < children.length; i++) {
                if (group.getName().equals(children[i].getName())) {
                    return (TreeParent) children[i];
                }
            }
            return null;
        }

        public TreeParent fillPresence(TreeParent obj, IPresence presence) {
            if (presence == null)
                return obj;
            TreeObject type = new TreeObject("Status: "
                    + presence.getType().toString());
            obj.addChild(type);
            String status = presence.getStatus();
            if (status != null && !status.equals("")) {
                TreeObject stat = new TreeObject("Status Details: " + status);
                obj.addChild(stat);
            }
            int priority = presence.getPriority();
            if (priority != -1) {
                TreeObject prior = new TreeObject("Priority: " + priority);
                obj.addChild(prior);
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

        public TreeParent fillWithEntry(TreeParent obj, IRosterEntry entry) {
            obj.removeChildren();
            String name = entry.getName();
            if (name != null) {
                obj.addChild(new TreeObject("Name: " + name));
            }
            return fillPresence(obj, entry.getPresenceState());
        }

        public void addEntry(TreeParent parent, IRosterEntry entry) {
            TreeObject[] objs = parent.getChildren();
            TreeParent found = null;
            if (objs != null) {
                for (int i = 0; i < objs.length; i++) {
                    if (objs[i].getName().equals(entry.getUserID().getName())) {
                        // Found it...replace values with new
                        found = fillWithEntry((TreeParent) objs[i], entry);
                    }
                }
            }
            if (found == null) {
                found = new TreeParent(entry.getUserID().getName(), entry
                        .getUserID());
                found = fillWithEntry(found, entry);
                parent.addChild(found);
            } else {
                parent.removeChild(found);
                parent.addChild(found);
            }
        }

        public TreeParent addEntriesToGroup(TreeParent grp, IRosterGroup group) {
            Iterator i = group.getRosterEntries();
            for (; i.hasNext();) {
                IRosterEntry entry = (IRosterEntry) i.next();
                if (entry != null) {
                    addEntry(grp, entry);
                }
            }
            return grp;
        }

        public void addEntry(IRosterEntry entry) {
            addEntry(root, entry);
        }

        public void addGroup(IRosterGroup group) {
            TreeParent grp = hasGroup(group);
            if (grp == null) {
                // Need to add it
                grp = new TreeParent(group.getName());
            }
            grp = addEntriesToGroup(grp, group);
            root.addChild(grp);
        }
        public void removeAllEntries() {
            root = null;
        }
        private void initialize() {
            root = new TreeParent("Buddy List");
            /*
             * root.addChild(p1); root.addChild(p2);
             */
            invisibleRoot = new TreeParent("");
            invisibleRoot.addChild(root);
        }
    }

    class ViewLabelProvider extends LabelProvider {
        public String getText(Object obj) {
            return obj.toString();
        }

        public Image getImage(Object obj) {
            Image image = null;     //By default, no image exists for obj, but if found to be a specific instance, load from plugin repository.
            ImageRegistry registry = UiPlugin.getDefault().getImageRegistry();
            
            if (obj instanceof TreeParent) {
                image = registry.get(UiPluginConstants.DECORATION_USER);
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
        viewer.setAutoExpandLevel(2);
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
        manager.add(chatAction);
        manager.add(new Separator());
        manager.add(disconnectAction);
    }

    private void fillContextMenu(IMenuManager manager) {
        TreeObject treeObject = getSelectedTreeObject();
        final ID targetID = treeObject.getUserID();
        if (treeObject != null) {
            selectedChatAction = new Action() {
                public void run() {
                    openChatWindowForTarget(targetID);
                }
            };
            selectedChatAction.setText("Send IM to "+treeObject.getUserID().getName());
            selectedChatAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                    .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
            manager.add(selectedChatAction);
        }
        
        manager.add(new Separator());
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    protected TreeObject getSelectedTreeObject() {
        ISelection selection = viewer.getSelection();
        Object obj = ((IStructuredSelection) selection)
                .getFirstElement();
        TreeObject treeObject = (TreeObject) obj;
        return treeObject;
    }
    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(chatAction);
        manager.add(new Separator());
        manager.add(disconnectAction);
    }

    protected ID inputIMTarget() {
        InputDialog dlg = new InputDialog(getSite().getShell(),"Send IM","Please enter the Jabber ID of the person you would like to IM","",null);
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
        chatAction.setToolTipText("Send Instant Message");
        chatAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
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
                if (textInputHandler != null) {
                    textInputHandler.disconnect();
                    chatAction.setEnabled(false);
                    this.setEnabled(false);
                }
            }
        };
        disconnectAction.setText("Disconnect");
        disconnectAction.setToolTipText("Disconnect from server");
        disconnectAction.setEnabled(false);
        
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

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.ui.presence.IRosterViewer#receiveRosterEntry(org.eclipse.ecf.ui.presence.IRosterEntry)
     */
    public void receiveRosterEntry(IRosterEntry entry) {
        if (entry == null)
            return;
        ViewContentProvider vcp = (ViewContentProvider) viewer
                .getContentProvider();
        if (vcp != null) {
            vcp.addEntry(entry);
            refreshView();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.ui.presence.IPresenceViewer#receivePresence(org.eclipse.ecf.core.identity.ID,
     *      org.eclipse.ecf.ui.presence.IPresence)
     */
    public void receivePresence(ID userID, IPresence presence) {
        IRosterEntry entry = new RosterEntry(userID, null, presence);
        receiveRosterEntry(entry);
        refreshView();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.ui.views.IConfigViewer#setUser(org.eclipse.ecf.core.user.IUser)
     */
    public void setLocalUser(IUser user, ITextInputHandler textInputHandler) {
        this.localUser = user;
        this.textInputHandler = textInputHandler;
    }

    public Object getAdapter(Class clazz) {
        if (clazz != null && clazz.equals(ITextInputHandler.class)) {
            return new ITextInputHandler() {
                public void handleTextLine(ID userID, String text) {
                    if (textInputHandler != null) {
                        textInputHandler.handleTextLine(userID, text);
                    } else
                        System.out.println("handleTextLine(" + text + ")");
                }

                public void handleStartTyping(ID userID) {
                    if (textInputHandler != null) {
                        textInputHandler.handleStartTyping(userID);
                    } else
                        System.out.println("handleStartTyping()");
                }
                public void disconnect() {
                    if (textInputHandler != null) {
                        textInputHandler.disconnect();
                    } else
                        System.out.println("disconnect()");
                }
            };
        } else if (clazz.equals(IConfigViewer.class)) {
            return this;
        } else if (clazz.equals(IRosterViewer.class)) {
            return this;
        } else if (clazz.equals(IMessageViewer.class)) {
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

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.ui.messaging.IMessageViewer#showMessage(org.eclipse.ecf.core.identity.ID,
     *      org.eclipse.ecf.core.identity.ID,
     *      org.eclipse.ecf.ui.messaging.IMessageViewer.Type, java.lang.String,
     *      java.lang.String)
     */
    public void showMessage(ID fromID, ID toID, Type type, String subject,
            String message) {
        ChatWindow window = openChatWindowForTarget(fromID);
        // finally, show message
        if (window != null) {
            window.showMessage(fromID, toID, type, subject, message);
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
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.ui.views.IConfigViewer#memberDeparted(org.eclipse.ecf.core.identity.ID)
     */
    public void memberDeparted(ID member) {
        if (groupID != null) {
            if (groupID.equals(member)) {
                handleGroupManagerDeparted();
            }
        }
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

    protected void handleGroupManagerDeparted() {
        removeAllRosterEntries();
        disposeAllChatWindows("Disconnected from server.  Chat is inactive");
        chatAction.setEnabled(false);
        disconnectAction.setEnabled(false);
    }

}