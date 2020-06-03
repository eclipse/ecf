/****************************************************************************
 * Copyright (c) 2008 Versant Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Remy Chi Jian Suen (Versant Corporation) - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.team.internal.ecf.ui.handlers;

import org.eclipse.ecf.presence.roster.IRosterEntry;
import org.eclipse.ecf.presence.ui.menu.AbstractRosterMenuContributionItem;
import org.eclipse.ecf.presence.ui.menu.AbstractRosterMenuHandler;
import org.eclipse.team.internal.ecf.ui.Messages;

public class SynchronizeWithMenuContributionItem extends AbstractRosterMenuContributionItem {

	public SynchronizeWithMenuContributionItem() {
		setTopMenuName(Messages.SynchronizeWithMenuContributionItem_MenuTitle);
	}

	protected AbstractRosterMenuHandler createRosterEntryHandler(IRosterEntry rosterEntry) {
		return new SynchronizeWithHandler(rosterEntry);
	}

}
