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

package org.eclipse.ecf.presence.ui.menu;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ecf.presence.roster.IRosterEntry;

/**
 *
 */
public class NoopRosterMenuContributionItem extends AbstractRosterMenuContributionItem {

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.presence.ui.menu.AbstractRosterMenuContributionItem#createRosterEntryHandler(org.eclipse.ecf.presence.roster.IRosterEntry)
	 */
	protected AbstractRosterMenuHandler createRosterEntryHandler(IRosterEntry rosterEntry) {
		return new AbstractRosterMenuHandler(rosterEntry) {
			public Object execute(ExecutionEvent arg0) {
				System.out.println("execute(" + arg0 + ") on rosterEntry=" + getRosterEntry()); //$NON-NLS-1$ //$NON-NLS-2$
				return null;
			}

		};
	}

}
