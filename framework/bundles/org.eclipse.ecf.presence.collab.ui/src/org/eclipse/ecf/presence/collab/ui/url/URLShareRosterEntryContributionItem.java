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
import org.eclipse.ecf.datashare.IChannelContainerAdapter;
import org.eclipse.ecf.internal.presence.collab.ui.Activator;
import org.eclipse.ecf.internal.presence.collab.ui.Messages;
import org.eclipse.ecf.presence.roster.IRosterEntry;
import org.eclipse.ecf.presence.ui.roster.AbstractRosterEntryContributionItem;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class URLShareRosterEntryContributionItem extends AbstractRosterEntryContributionItem {

	public URLShareRosterEntryContributionItem() {
		// do nothing
	}

	public URLShareRosterEntryContributionItem(String id) {
		super(id);
	}

	protected IAction[] makeActions() {
		// Else check for Roster entry
		final IRosterEntry entry = getSelectedRosterEntry();
		final IContainer c = getContainerForRosterEntry(entry);
		// If roster entry is selected and it has a container
		if (entry != null && c != null) {
			final IChannelContainerAdapter channelAdapter = (IChannelContainerAdapter) c.getAdapter(IChannelContainerAdapter.class);
			// If the container has channel container adapter and is online/available
			if (channelAdapter != null && isAvailable(entry)) {
				final URLShare tmp = URLShare.getURLShare(c.getID());
				// If there is an URL share associated with this container
				if (tmp != null) {
					final URLShare urlshare = tmp;
					final IAction action = new Action() {
						public void run() {
							urlshare.showDialogAndSendURL(entry.getRoster().getUser().getName(), entry.getUser().getID());
						}
					};
					action.setText(Messages.URLShareRosterEntryContributionItem_SEND_URL_MENU_TEXT);
					action.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, Messages.URLShareRosterContributionItem_BROWSER_ICON));
					return new IAction[] {action};
				}
			}
		}
		return null;
	}

}
