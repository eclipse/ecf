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
package org.eclipse.ecf.example.collab.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.sharedobject.ISharedObject;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainer;
import org.eclipse.ecf.example.collab.ClientPlugin;
import org.eclipse.ecf.example.collab.share.RosterSharedObject;
import org.eclipse.ecf.ui.views.RosterBuddy;
import org.eclipse.ecf.ui.views.RosterGroup;
import org.eclipse.ecf.ui.views.RosterObject;
import org.eclipse.ecf.ui.views.RosterParent;
import org.eclipse.ecf.ui.views.RosterView;
import org.eclipse.ecf.ui.views.RosterUserAccount;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.views.IViewCategory;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

public class CollabRosterView extends RosterView {

	RosterSharedObject sharedObject = null;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.ui.views.RosterView#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
		final RosterObject rosterObject = getSelectedTreeObject();
		if (rosterObject != null && rosterObject instanceof RosterBuddy) {
			RosterBuddy buddy = (RosterBuddy) rosterObject;
			RosterParent group = rosterObject.getParent();
			final RosterUserAccount ua = getAccount(buddy.getServiceID());
			Action sendSOMessageAction = new Action() {
				public void run() {
					RosterSharedObject so = (RosterSharedObject) ua
							.getSharedObject();
					if (so != null) {
						InputDialog dialog = new InputDialog(getSite().getShell(),"Send ECF Private Message","Private Message: ",null,null);
						dialog.open();
						if (dialog.getReturnCode() == InputDialog.OK) {
							String message = dialog.getValue();
							so.sendPrivateMessageTo(rosterObject.getID(), ua.getUser().getName(), message);
						}
					}
				}
			};
			sendSOMessageAction.setText("(ECF) Send Private Message to "
					+ rosterObject.getID().getName());
			sendSOMessageAction.setEnabled(ua.getSharedObject() != null);
			manager.add(sendSOMessageAction);

			Action sendShowViewAction = new Action() {
				public void run() {
					sendShowViewRequest(rosterObject.getID());
				}
			};
			sendShowViewAction.setText("(ECF) Remote Open View for "
					+ rosterObject.getID().getName());
			sendShowViewAction.setEnabled(ua.getSharedObject() != null);
			manager.add(sendShowViewAction);

			// Setup action for reporting active and total group size
			int activeGroupSize = 0;
			int totalGroupSize = 0;
			String gn = "";
			if (group instanceof RosterGroup) {
				RosterGroup tg = (RosterGroup) group;
				totalGroupSize = tg.getTotalCount();
				activeGroupSize = tg.getActiveCount();
				gn = tg.getName();
			}
			final Integer activeSize = new Integer(activeGroupSize);
			final Integer totalSize = new Integer(totalGroupSize);
			final String groupName = gn;
			Action sendSOGroupSizeAction = new Action() {
				public void run() {
					RosterSharedObject so = (RosterSharedObject) ua
							.getSharedObject();
					if (so != null) {
						so.sendGroupSizeMessageTo(rosterObject.getID(), ua.getUser().getName(), groupName, activeSize, totalSize);
					}
				}
			};
			sendSOGroupSizeAction.setText("(ECF) Send Group Meta-Information to "
					+ rosterObject.getID().getName());
			sendSOGroupSizeAction.setEnabled(ua.getSharedObject() != null);
			manager.add(sendSOGroupSizeAction);

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.ui.views.RosterView#createAndAddSharedObjectForAccount(org.eclipse.ecf.ui.views.RosterView.UserAccount)
	 */
	protected ISharedObject createAndAddSharedObjectForAccount(
			RosterUserAccount account) {
		ISharedObjectContainer container = account.getSOContainer();
		if (container != null) {
			try {
				sharedObject = new RosterSharedObject(this);
				container.getSharedObjectManager().addSharedObject(
						IDFactory.getDefault().createStringID(
								RosterSharedObject.class.getName()),
						sharedObject, null);
				return sharedObject;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else
			return null;
	}

	private void sendShowViewRequest(ID userID) {
		IWorkbenchWindow ww = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage page = ww.getActivePage();
		if (page == null)
			return;

		ElementTreeSelectionDialog dlg = new ElementTreeSelectionDialog(
				this.getViewSite().getShell(), 
				new LabelProvider() {
					private HashMap images = new HashMap();
					public Image getImage(Object element) {
						ImageDescriptor desc = null;
						if (element instanceof IViewCategory)
							desc = PlatformUI.getWorkbench().getSharedImages()
									.getImageDescriptor(
											ISharedImages.IMG_OBJ_FOLDER);
						else if (element instanceof IViewDescriptor)
							desc = ((IViewDescriptor) element).getImageDescriptor();

						if (desc == null)
							return null;
						
						Image image = (Image) images.get(desc);
						if (image == null) {
							image = desc.createImage();
							images.put(desc, image);
						}
						
						return image;
					}
					public String getText(Object element) {
						String label;
						if (element instanceof IViewCategory)
							label = ((IViewCategory) element).getLabel();
						else if (element instanceof IViewDescriptor)
							label = ((IViewDescriptor) element).getLabel();
						else
							label = super.getText(element);
						
						for (
								int i = label.indexOf('&'); 
								i >= 0 && i < label.length() - 1; 
								i = label.indexOf('&', i + 1))
							if (!Character.isWhitespace(label.charAt(i + 1)))
								return label.substring(0, i) + label.substring(i + 1);
						
						return label;
					}
					public void dispose() {
						for (Iterator i = images.values().iterator(); i.hasNext();)
							((Image) i.next()).dispose();

						images = null;
						super.dispose();
					}
				}, 
				new ITreeContentProvider() {
					private HashMap parents = new HashMap();
					public Object[] getChildren(Object element) {
						if (element instanceof IViewRegistry)
							return ((IViewRegistry) element).getCategories();
						else if (element instanceof IViewCategory) {
							IViewDescriptor[] children =
								((IViewCategory) element).getViews();
							for (int i = 0; i < children.length; ++i)
								parents.put(children[i], element);
							
							return children; 
						} else
							return new Object[0];
					}
					public Object getParent(Object element) {
						if (element instanceof IViewCategory)
							return PlatformUI.getWorkbench().getViewRegistry();
						else if (element instanceof IViewDescriptor)
							return parents.get(element);
						else
							return null;
					}
					public boolean hasChildren(Object element) {
						if (element instanceof IViewRegistry
								|| element instanceof IViewCategory)
							return true;
						else
							return false;
					}
					public Object[] getElements(Object inputElement) {
						return getChildren(inputElement);
					}
					public void dispose() {
						parents = null;
					}
					public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
						parents.clear();
					}
				});
		dlg.setTitle(MessageLoader
				.getString("LineChatClientView.contextmenu.sendShowViewRequest"));
		dlg.setMessage(MessageLoader
				.getString("LineChatClientView.contextmenu.sendShowViewRequest.dialog.title"));
		dlg.addFilter(new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof IViewDescriptor
						&& "org.eclipse.ui.internal.introview".equals(
								((IViewDescriptor) element).getId()))
					return false;
				else
					return true;
			}});
		dlg.setSorter(new ViewerSorter());
		dlg.setValidator(new ISelectionStatusValidator() {
			public IStatus validate(Object[] selection) {
				for (int i = 0; i < selection.length; ++i)
					if (!(selection[i] instanceof IViewDescriptor))
						return new Status(Status.ERROR, ClientPlugin.getDefault().getBundle().getSymbolicName(), 0, "", null);
				
				return new Status(Status.OK, ClientPlugin.getDefault().getBundle().getSymbolicName(), 0, "", null);
			}
		});
		IViewRegistry reg = PlatformUI.getWorkbench().getViewRegistry(); 
		dlg.setInput(reg);
		IDialogSettings dlgSettings = ClientPlugin.getDefault().getDialogSettings();
		final String DIALOG_SETTINGS = "SendShowViewRequestDialog";
		final String SELECTION_SETTING = "SELECTION";
		IDialogSettings section = dlgSettings.getSection(DIALOG_SETTINGS);
		if (section == null)
			section = dlgSettings.addNewSection(DIALOG_SETTINGS);
		else {
			String[] selectedIDs = section.getArray(SELECTION_SETTING);
			if (selectedIDs != null && selectedIDs.length > 0) {
				ArrayList list = new ArrayList(selectedIDs.length);
				for (int i = 0; i < selectedIDs.length; ++i) {
					IViewDescriptor desc = reg.find(selectedIDs[i]);
					if (desc != null)
						list.add(desc);
				}
				
				dlg.setInitialElementSelections(list);
			}
		}

		dlg.open();
		if (dlg.getReturnCode() == Window.CANCEL)
			return;

		Object[] descs = dlg.getResult();
		if (descs == null)
			return;
		
		String[] selectedIDs = new String[descs.length];
		for (int i = 0; i < descs.length; ++i) {
			selectedIDs[i] = ((IViewDescriptor)descs[i]).getId();
			if (sharedObject != null) {
				sharedObject.sendShowView(userID, selectedIDs[i]);
			}
		}
		
		section.put(SELECTION_SETTING, selectedIDs);
	}

}
