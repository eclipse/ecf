package org.eclipse.ecf.example.collab.ui;

import org.eclipse.ecf.core.ISharedObject;
import org.eclipse.ecf.core.ISharedObjectContainer;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.example.collab.share.RosterSharedObject;
import org.eclipse.ecf.ui.views.RosterView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;

public class CollabRosterView extends RosterView {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.ui.views.RosterView#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
		final TreeObject treeObject = getSelectedTreeObject();
		if (treeObject != null && treeObject instanceof TreeBuddy) {
			final UserAccount ua = getAccount(((TreeBuddy) treeObject)
					.getServiceID());
			Action sendSOMessageAction = new Action() {
				public void run() {
					RosterSharedObject so = (RosterSharedObject) ua
							.getSharedObject();
					if (so != null) {
						so.sendMessageTo(treeObject.getId(), "hello!");
					}
				}
			};
			sendSOMessageAction.setText("Send shared object hello message to "
					+ treeObject.getId().getName());
			sendSOMessageAction.setEnabled(ua.getSharedObject() != null);
			manager.add(sendSOMessageAction);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.ui.views.RosterView#createAndAddSharedObjectForAccount(org.eclipse.ecf.ui.views.RosterView.UserAccount)
	 */
	protected ISharedObject createAndAddSharedObjectForAccount(
			UserAccount account) {
		ISharedObjectContainer container = account.getSOContainer();
		if (container != null) {
			try {
				ISharedObject sharedObject = new RosterSharedObject(this);
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

}
