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
package org.eclipse.ecf.presence.collab.ui.console;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.datashare.IChannelContainerAdapter;
import org.eclipse.ecf.presence.roster.IRosterEntry;
import org.eclipse.ecf.presence.ui.roster.AbstractRosterEntryContributionItem;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.TextSelection;

public class StackShareRosterEntryContributionItem extends AbstractRosterEntryContributionItem {

	public StackShareRosterEntryContributionItem() {
	}

	public StackShareRosterEntryContributionItem(String id) {
		super(id);
	}

	protected IAction[] makeActions() {
		final TextSelection selection = StackShare.getSelection();
		if (selection == null)
			return null;
		// Else check for Roster entry
		final IRosterEntry entry = getSelectedRosterEntry();
		final IContainer c = getContainerForRosterEntry(entry);
		// If roster entry is selected and it has a container
		if (entry != null && c != null) {
			final IChannelContainerAdapter channelAdapter = (IChannelContainerAdapter) c.getAdapter(IChannelContainerAdapter.class);
			// If the container has channel container adapter and is online/available
			if (channelAdapter != null && isAvailable(entry)) {
				final StackShare tmp = StackShare.getStackShare(c.getID());
				// If there is an URL share associated with this container
				if (tmp != null) {
					final StackShare stackshare = tmp;
					final IAction action = new Action() {
						public void run() {
							stackshare.sendShareStackRequest(entry.getRoster().getUser().getName(), entry.getUser().getID(), selection.getText());
						}
					};
					action.setText(Messages.getString("StackShareRosterEntryContributionItem.SEND_STACK_TRACE_MENU")); //$NON-NLS-1$
					//action.setImageDescriptor(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, Messages.URLShareRosterContributionItem_BROWSER_ICON));
					return new IAction[] {action};
				}
			}
		}
		return null;
	}
}
