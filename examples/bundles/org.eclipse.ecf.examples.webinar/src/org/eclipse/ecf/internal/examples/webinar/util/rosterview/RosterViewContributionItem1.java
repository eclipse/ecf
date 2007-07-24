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

package org.eclipse.ecf.internal.examples.webinar.util.rosterview;

import org.eclipse.ecf.internal.examples.webinar.util.RosterWriter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.actions.CompoundContributionItem;

/**
 *
 */
public class RosterViewContributionItem1 extends CompoundContributionItem {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.CompoundContributionItem#getContributionItems()
	 */
	protected IContributionItem[] getContributionItems() {
		return new IContributionItem[] { new ActionContributionItem(getAction()) };
	}

	private IAction getAction() {
		IAction action = new Action() {
			public void run() {
				System.out.println("showing rosters on console");
				RosterWriter writer = new RosterWriter();
				writer.showAllRosters();
			}
		};
		action.setText("show all rosters on console");
		return action;
	}
}
