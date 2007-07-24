package org.eclipse.ecf.internal.examples.webinar;

import org.eclipse.ecf.presence.roster.IRosterEntry;
import org.eclipse.ecf.presence.ui.roster.AbstractRosterEntryContributionItem;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class RosterEntryContributionItem1 extends AbstractRosterEntryContributionItem {

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.presence.ui.roster.AbstractPresenceContributionItem#makeActions()
	 */
	protected IAction[] makeActions() {
		IAction action = null;
		IRosterEntry entry = getSelectedRosterEntry();
		if (entry != null) {
			action = new Action() {
				public void run() {
					IRosterEntry rosterEntry = getSelectedRosterEntry();
					System.out.println("running action for roster entry "+rosterEntry.getName());
				}
			};
			action.setText("Send info to "+getSelectedRosterEntry().getName());
			action.setImageDescriptor(PlatformUI.getWorkbench()
					.getSharedImages().getImageDescriptor(
							ISharedImages.IMG_DEF_VIEW));
			return new IAction[] { action };
		}
		return null;
	}

}
