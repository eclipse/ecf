package org.eclipse.ecf.ui.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.ui.presence.IPresence;
import org.eclipse.ecf.ui.presence.IPresenceViewer;
import org.eclipse.ecf.ui.presence.IRosterEntry;
import org.eclipse.ecf.ui.presence.IRosterGroup;
import org.eclipse.ecf.ui.presence.IRosterViewer;
import org.eclipse.ecf.ui.presence.RosterEntry;

/**
 * WARNING:  IN PROGRESS
 *
 */
public class RosterView extends ViewPart implements IRosterViewer, IPresenceViewer {
	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action action1;
	private Action doubleClickAction;

	class TreeObject implements IAdaptable {
		private String name;
		private TreeParent parent;
		
		public TreeObject(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
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
		public void addChild(TreeObject child) {
			children.add(child);
			child.setParent(this);
		}
		public void removeChild(TreeObject child) {
			children.remove(child);
			child.setParent(null);
		}
        public void removeChildren() {
            for(Iterator i=children.iterator(); i.hasNext(); ) {
                TreeObject obj = (TreeObject) i.next();
                obj.setParent(null);
            }
            children.clear();
        }
		public TreeObject [] getChildren() {
			return (TreeObject [])children.toArray(new TreeObject[children.size()]);
		}
		public boolean hasChildren() {
			return children.size()>0;
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
				if (invisibleRoot==null) initialize();
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}
		public Object getParent(Object child) {
			if (child instanceof TreeObject) {
				return ((TreeObject)child).getParent();
			}
			return null;
		}
		public Object [] getChildren(Object parent) {
			if (parent instanceof TreeParent) {
				return ((TreeParent)parent).getChildren();
			}
			return new Object[0];
		}
		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeParent)
				return ((TreeParent)parent).hasChildren();
			return false;
		}
        public TreeParent hasGroup(IRosterGroup group) {
            if (group == null) return null;
            TreeObject [] children = root.getChildren();
            if (children == null) return null;
            for(int i=0; i < children.length; i++) {
                if (group.getName().equals(children[i].getName())) {
                    return (TreeParent) children[i];
                }
            }
            return null;
        }
        
        public TreeParent fillPresence(TreeParent obj, IPresence presence) {
            if (presence == null) return obj;
            TreeObject type = new TreeObject(presence.getType().toString());
            obj.addChild(type);
            TreeObject mode = new TreeObject("Mode: "+presence.getMode().toString());
            obj.addChild(mode);
            String status = presence.getStatus();
            if (status != null && !status.equals("")) {
                TreeObject stat = new TreeObject("Status: "+status);
                obj.addChild(stat);
            }
            int priority = presence.getPriority();
            if (priority != -1) {
                TreeObject prior = new TreeObject("Priority: "+priority);
                obj.addChild(prior);
            }
            Map props = presence.getProperties();
            for(Iterator i=props.keySet().iterator(); i.hasNext(); ) {
                String key = (String) i.next();
                String value = (String) props.get(key);
                if (key != null && value != null) {
                    TreeObject prop = new TreeObject(key+": "+value);
                    obj.addChild(prop);
                }
            }
            return obj;
        }
        public TreeParent fillWithEntry(TreeParent obj, IRosterEntry entry) {
            obj.removeChildren();
            String name = entry.getName();
            if (name != null) {
                obj.addChild(new TreeObject("Name: "+name));
            }
            obj.addChild(new TreeObject("User ID: "+entry.getUserID().getName()));
            return fillPresence(obj,entry.getPresenceState());
        }
        public void addEntry(TreeParent parent, IRosterEntry entry) {
            TreeObject [] objs = parent.getChildren();
            TreeParent found = null;
            if (objs != null) {
                for(int i=0; i < objs.length; i++) {
                    if (objs[i].getName().equals(entry.getName())) {
                        // Found it...replace values with new 
                        found = fillWithEntry((TreeParent) objs[i],entry);
                    }
                }
            }
            if (found == null) {
                found = new TreeParent(entry.getUserID().getName());
                found = fillWithEntry(found,entry);
            }
            parent.addChild(found);
        }
        public TreeParent addEntriesToGroup(TreeParent grp, IRosterGroup group) {
            Iterator i = group.getRosterEntries();
            for( ; i.hasNext(); ) {
                IRosterEntry entry = (IRosterEntry) i.next();
                if (entry != null) {
                    addEntry(grp,entry);
                }
            }
            return grp;
        }
        
        public void addEntry(IRosterEntry entry) {
            addEntry(root,entry);
            viewer.expandAll();
        }
		public void addGroup(IRosterGroup group) {
            TreeParent grp = hasGroup(group);
            if (grp == null) {
                // Need to add it
                grp = new TreeParent(group.getName());
            }
            grp = addEntriesToGroup(grp,group);
            root.addChild(grp);
        }
        private void initialize() {
            /*
			TreeObject to1 = new TreeObject("Leaf 1");
			TreeObject to2 = new TreeObject("Leaf 2");
			TreeObject to3 = new TreeObject("Leaf 3");
			TreeParent p1 = new TreeParent("Parent 1");
			p1.addChild(to1);
			p1.addChild(to2);
			p1.addChild(to3);
			
			TreeObject to4 = new TreeObject("Leaf 4");
			TreeParent p2 = new TreeParent("Parent 2");
			p2.addChild(to4);
			*/
            root = new TreeParent("Buddies");
            /*
			root.addChild(p1);
			root.addChild(p2);
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
			String imageKey = null;
			if (obj instanceof TreeParent)
			   imageKey = ISharedImages.IMG_OBJ_FOLDER;
			return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}
	}
	class NameSorter extends ViewerSorter {
	}

	public RosterView() {
	}

	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);
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
		manager.add(action1);
		manager.add(new Separator());
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				showMessage("Double-click detected on "+obj.toString());
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Roster View",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

    /* (non-Javadoc)
     * @see org.eclipse.ecf.ui.presence.IRosterViewer#receiveRosterEntry(org.eclipse.ecf.ui.presence.IRosterEntry)
     */
    public void receiveRosterEntry(IRosterEntry entry) {
        System.out.println("Received roster entry: "+entry);
        if (entry == null) return;
        ViewContentProvider vcp = (ViewContentProvider) viewer.getContentProvider();
        vcp.addEntry(entry);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.ui.presence.IPresenceViewer#receivePresence(org.eclipse.ecf.core.identity.ID, org.eclipse.ecf.ui.presence.IPresence)
     */
    public void receivePresence(ID userID, IPresence presence) {
        IRosterEntry entry = new RosterEntry(userID,null,presence);
        receiveRosterEntry(entry);
    }
}