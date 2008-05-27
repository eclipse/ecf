/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.docshare.menu;

import java.util.*;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.docshare.DocShare;
import org.eclipse.ecf.internal.docshare.Activator;
import org.eclipse.ecf.internal.docshare.Messages;
import org.eclipse.ecf.presence.IPresenceContainerAdapter;
import org.eclipse.ecf.presence.roster.IRoster;
import org.eclipse.ecf.presence.roster.IRosterEntry;
import org.eclipse.ecf.presence.ui.menu.AbstractRosterMenuContributionItem;
import org.eclipse.ecf.presence.ui.menu.AbstractRosterMenuHandler;
import org.eclipse.jface.action.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.*;
import org.eclipse.ui.texteditor.ITextEditor;

public class DocShareRosterMenuContributionItem extends AbstractRosterMenuContributionItem {

	public DocShareRosterMenuContributionItem() {
		super();
		setTopMenuName(Messages.DocShareRosterMenuContributionItem_SHARE_EDITOR_MENU_TEXT);
	}

	public DocShareRosterMenuContributionItem(String id) {
		super(id);
		setTopMenuName(Messages.DocShareRosterMenuContributionItem_SHARE_EDITOR_MENU_TEXT);
	}

	protected IEditorPart getEditorPart() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return null;
		final IWorkbenchWindow ww = workbench.getActiveWorkbenchWindow();
		if (ww == null)
			return null;
		final IWorkbenchPage wp = ww.getActivePage();
		if (wp == null)
			return null;
		return wp.getActiveEditor();
	}

	protected DocShare getDocShareForPresenceContainerAdapter(IPresenceContainerAdapter presenceContainerAdapter) {
		final IContainer container = (IContainer) presenceContainerAdapter.getAdapter(IContainer.class);
		if (container == null)
			return null;
		return Activator.getDefault().getDocShare(container.getID());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.presence.ui.menu.AbstractRosterMenuContributionItem#getContributionItems()
	 */
	protected IContributionItem[] getContributionItems() {
		clearOldContributions();
		// Make sure this is a text editor
		final IEditorPart editorPart = getEditorPart();
		if (editorPart == null || !(editorPart instanceof ITextEditor))
			return NO_CONTRIBUTIONS;
		// If we are already engaged in a doc share (either as initiator or as receiver)
		// Then present menu item to stop
		final List presenceContainerAdapters = getPresenceContainerAdapters();
		for (final Iterator i = presenceContainerAdapters.iterator(); i.hasNext();) {
			final IPresenceContainerAdapter pca = (IPresenceContainerAdapter) i.next();
			final DocShare docShare = getDocShareForPresenceContainerAdapter(pca);
			if (docShare != null && docShare.isSharing() && docShare.getTextEditor().equals(editorPart)) {
				return getMenuContributionsDuringShare(docShare);
			}
		}
		return super.getContributionItems();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.presence.ui.menu.AbstractRosterMenuContributionItem#createContributionItemsForPresenceContainer(org.eclipse.ecf.presence.IPresenceContainerAdapter)
	 */
	protected IContributionItem[] createContributionItemsForPresenceContainer(IPresenceContainerAdapter presenceContainerAdapter) {
		final IContainer container = (IContainer) presenceContainerAdapter.getAdapter(IContainer.class);
		if (container == null)
			return NO_CONTRIBUTIONS;
		final DocShare docShare = Activator.getDefault().getDocShare(container.getID());
		if (docShare == null)
			return NO_CONTRIBUTIONS;
		final IRoster roster = presenceContainerAdapter.getRosterManager().getRoster();
		final IContributionItem[] contributions = createContributionItemsForRoster(roster);
		if (contributions == null || contributions.length == 0)
			return NO_CONTRIBUTIONS;
		final MenuManager menuManager = createMenuManagerForRoster(roster);
		for (int i = 0; i < contributions.length; i++) {
			menuManager.add(contributions[i]);
		}
		return new IContributionItem[] {menuManager};
	}

	protected IContributionItem[] getMenuContributionsDuringShare(final DocShare docShare) {
		List items = new ArrayList();
		if (docShare.isInitiator()) {
			items.add(new Separator());
		}
		final IAction sendSelection = new Action() {
			public void run() {
				docShare.sendSelection();
			}
		};
		sendSelection.setText(NLS.bind(Messages.DocShareRosterMenuContributionItem_SELECTION_SEND_EDITOR_MENU_TEXT, trimIDNameForMenu(docShare.getOtherID())));
		items.add(new ActionContributionItem(sendSelection));
		final IAction stopEditorShare = new Action() {
			public void run() {
				docShare.stopShare();
			}
		};
		stopEditorShare.setText(NLS.bind(Messages.DocShareRosterMenuContributionItem_STOP_SHARE_EDITOR_MENU_TEXT, trimIDNameForMenu(docShare.getOtherID())));
		items.add(new ActionContributionItem(stopEditorShare));
		return (IContributionItem[]) items.toArray(new IContributionItem[] {});
	}

	protected AbstractRosterMenuHandler createRosterEntryHandler(IRosterEntry rosterEntry) {
		return new DocShareRosterMenuHandler(rosterEntry);
	}

	protected String trimIDNameForMenu(ID id) {
		final String idName = id.getName();
		final int indexAt = idName.indexOf("@"); //$NON-NLS-1$
		if (indexAt == -1)
			return idName;
		return idName.substring(0, indexAt);
	}
}
