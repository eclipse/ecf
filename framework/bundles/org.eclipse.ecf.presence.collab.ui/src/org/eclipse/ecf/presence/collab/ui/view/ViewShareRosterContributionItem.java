/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.presence.collab.ui.view;

import java.util.Hashtable;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.datashare.IChannelContainerAdapter;
import org.eclipse.ecf.internal.presence.collab.ui.Messages;
import org.eclipse.ecf.presence.roster.IRoster;
import org.eclipse.ecf.presence.ui.roster.AbstractRosterContributionItem;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class ViewShareRosterContributionItem extends
		AbstractRosterContributionItem {

	private static Hashtable viewSharechannels = new Hashtable();

	public ViewShareRosterContributionItem() {
	}

	public ViewShareRosterContributionItem(String id) {
		super(id);
	}

	protected static ViewShare getViewShare(ID containerID) {
		return (ViewShare) viewSharechannels.get(containerID);
	}

	protected static ViewShare addViewShare(ID containerID, ViewShare ViewShare) {
		return (ViewShare) viewSharechannels.put(containerID, ViewShare);
	}

	protected static ViewShare removeViewShare(ID containerID) {
		return (ViewShare) viewSharechannels.remove(containerID);
	}

	private IAction[] createActionAdd(final ID containerID,
			final IChannelContainerAdapter channelAdapter) {
		IAction action = new Action() {
			public void run() {
				try {
					addViewShare(containerID, new ViewShare(containerID, channelAdapter));
				} catch (ECFException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		action
				.setText(Messages.ViewShareRosterContributionItem_VIEWSHARE_LISTENER_MENU_ADD_TEXT);
		action.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_DEF_VIEW));
		return new IAction[] { action };
	}

	private IAction[] createActionRemove(final ID containerID,
			final ViewShare viewShare) {
		IAction action = new Action() {
			public void run() {
				removeViewShare(containerID);
				viewShare.dispose();
			}
		};
		action
				.setText(Messages.ViewShareRosterContributionItem_VIEWSHARE_LISTENER_MENU_REMOVE_TEXT);
		return new IAction[] { action };
	}

	protected IAction[] makeActions() {
		final IRoster roster = getSelectedRoster();
		if (roster != null) {
			// Roster is selected
			IContainer c = getContainerForRoster(roster);
			if (c != null) {
				// Get existing ViewShare for this container (if it exists)
				ViewShare viewShare = getViewShare(c.getID());
				// If it does exist already, then create action to remove
				if (viewShare != null)
					return createActionRemove(c.getID(), viewShare);
				else {
					IChannelContainerAdapter channelAdapter = (IChannelContainerAdapter) c
							.getAdapter(IChannelContainerAdapter.class);
					return (channelAdapter == null) ? null : createActionAdd(c
							.getID(), channelAdapter);
				}
			}
		}
		return null;
	}

}
