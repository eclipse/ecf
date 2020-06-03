/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.presence.collab.ui.url;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.datashare.IChannelContainerAdapter;
import org.eclipse.ecf.internal.presence.collab.ui.Activator;
import org.eclipse.ecf.internal.presence.collab.ui.Messages;
import org.eclipse.ecf.presence.roster.IRoster;
import org.eclipse.ecf.presence.ui.roster.AbstractRosterContributionItem;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class URLShareRosterContributionItem extends AbstractRosterContributionItem {

	public URLShareRosterContributionItem() {
		// do nothing
	}

	public URLShareRosterContributionItem(String id) {
		super(id);
	}

	private IAction[] createActionAdd(final ID containerID, final IChannelContainerAdapter channelAdapter) {
		final IAction action = new Action() {
			public void run() {
				try {
					URLShare.addURLShare(containerID, channelAdapter);
				} catch (ECFException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		action.setText(Messages.URLShareRosterContributionItem_ADD_URL_SHARE_MENU_TEXT);
		action.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, Messages.URLShareRosterContributionItem_BROWSER_ICON));
		return new IAction[] {action};
	}

	private IAction[] createActionRemove(final ID containerID, final URLShare urlshare) {
		final IAction action = new Action() {
			public void run() {
				URLShare.removeURLShare(containerID);
				urlshare.dispose();
			}
		};
		action.setText(Messages.URLShareRosterContributionItem_REMOVE_URL_SHARE_MENU_TEXT);
		action.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, Messages.URLShareRosterContributionItem_BROWSER_ICON));
		return new IAction[] {action};
	}

	protected IAction[] makeActions() {
		final IRoster roster = getSelectedRoster();
		if (roster != null) {
			// Roster is selected
			final IContainer c = getContainerForRoster(roster);
			if (c != null) {
				// Get existing urlshare for this container (if it exists)
				final URLShare urlshare = URLShare.getURLShare(c.getID());
				// If it does exist already, then create action to remove
				if (urlshare != null)
					return createActionRemove(c.getID(), urlshare);
				final IChannelContainerAdapter channelAdapter = (IChannelContainerAdapter) c.getAdapter(IChannelContainerAdapter.class);
				return (channelAdapter == null) ? null : createActionAdd(c.getID(), channelAdapter);
			}
		}
		return null;
	}

}
