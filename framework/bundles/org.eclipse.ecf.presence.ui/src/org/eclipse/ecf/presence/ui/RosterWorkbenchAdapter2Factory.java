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
package org.eclipse.ecf.presence.ui;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ecf.presence.roster.Roster;
import org.eclipse.ecf.presence.roster.RosterEntry;
import org.eclipse.ecf.presence.roster.RosterGroup;
import org.eclipse.ecf.presence.roster.RosterItem;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.model.IWorkbenchAdapter2;

public class RosterWorkbenchAdapter2Factory implements IAdapterFactory {

	private IWorkbenchAdapter2 rosterAdapter = new IWorkbenchAdapter2() {

		public RGB getBackground(Object element) {
			return getBackgroundForRoster((Roster) element);
		}

		public FontData getFont(Object element) {
			return getFontForRoster((Roster) element);
		}

		public RGB getForeground(Object element) {
			return getForegroundForRoster((Roster) element);
		}

	};

	private IWorkbenchAdapter2 rosterGroupAdapter = new IWorkbenchAdapter2() {

		public RGB getBackground(Object element) {
			return getBackgroundForRosterGroup((RosterGroup) element);
		}

		public FontData getFont(Object element) {
			return getFontForRosterGroup((RosterGroup) element);
		}

		public RGB getForeground(Object element) {
			return getForegroundForRosterGroup((RosterGroup) element);
		}

	};

	private IWorkbenchAdapter2 rosterItemAdapter = new IWorkbenchAdapter2() {

		public RGB getBackground(Object element) {
			return getBackgroundForRosterItem((RosterItem) element);
		}

		public FontData getFont(Object element) {
			return getFontForRosterItem((RosterItem) element);
		}

		public RGB getForeground(Object element) {
			return getForegroundForRosterItem((RosterItem) element);
		}

	};

	private IWorkbenchAdapter2 rosterEntryAdapter = new IWorkbenchAdapter2() {

		public RGB getBackground(Object element) {
			return getBackgroundForRosterEntry((RosterEntry) element);
		}

		public FontData getFont(Object element) {
			return getFontForRosterEntry((RosterEntry) element);
		}

		public RGB getForeground(Object element) {
			return getForegroundForRosterEntry((RosterEntry) element);
		}

	};

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType.equals(IWorkbenchAdapter2.class)) {
			if (adaptableObject instanceof Roster)
				return rosterAdapter;
			if (adaptableObject instanceof RosterGroup)
				return rosterGroupAdapter;
			if (adaptableObject instanceof RosterEntry)
				return rosterEntryAdapter;
			if (adaptableObject instanceof RosterItem)
				return rosterItemAdapter;
		}
		return null;
	}

	/**
	 * @param element to get foreground color for.  This implementation returns
	 * null, meaning that the default color will be used.  Subclasses should override as appropriate.
	 * @return RGB to use as foreground color.  If <code>null</code> use default color
	 */
	protected RGB getForegroundForRosterEntry(RosterEntry element) {
		return null;
	}

	/**
	 * @param element to get fontdata for.  This implementation returns
	 * null, meaning that the default font will be used.  Subclasses should override as appropriate.
	 * @return FontData to use for rendering given element.  If <code>null</code> use default FontData
	 */
	protected FontData getFontForRosterEntry(RosterEntry element) {
		return null;
	}

	/**
	 * @param element to get background color for.  This implementation returns
	 * null, meaning that the default color will be used.  Subclasses should override as appropriate.
	 * @return RGB to use as background color.  If <code>null</code> use default color
	 */
	protected RGB getBackgroundForRosterEntry(RosterEntry element) {
		return null;
	}

	/**
	 * @param element to get foreground color for.  This implementation returns
	 * null, meaning that the default color will be used.  Subclasses should override as appropriate.
	 * @return RGB to use as foreground color.  If <code>null</code> use default color
	 */
	protected RGB getForegroundForRosterItem(RosterItem element) {
		return null;
	}

	/**
	 * @param element to get fontdata for.  This implementation returns
	 * null, meaning that the default font will be used.  Subclasses should override as appropriate.
	 * @return FontData to use for rendering given element.  If <code>null</code> use default FontData
	 */
	protected FontData getFontForRosterItem(RosterItem element) {
		return null;
	}

	/**
	 * @param element to get background color for.  This implementation returns
	 * null, meaning that the default color will be used.  Subclasses should override as appropriate.
	 * @return RGB to use as background color.  If <code>null</code> use default color
	 */
	protected RGB getBackgroundForRosterItem(RosterItem element) {
		return null;
	}

	/**
	 * @param element to get foreground color for.  This implementation returns
	 * null, meaning that the default color will be used.  Subclasses should override as appropriate.
	 * @return RGB to use as foreground color.  If <code>null</code> use default color
	 */
	protected RGB getForegroundForRosterGroup(RosterGroup element) {
		return null;
	}

	/**
	 * @param element to get fontdata for.  This implementation returns
	 * null, meaning that the default font will be used.  Subclasses should override as appropriate.
	 * @return FontData to use for rendering given element.  If <code>null</code> use default FontData
	 */
	protected FontData getFontForRosterGroup(RosterGroup element) {
		return null;
	}

	/**
	 * @param element to get background color for.
	 * @return RGB to use as background color.  If <code>null</code> use default color
	 */
	protected RGB getBackgroundForRosterGroup(RosterGroup element) {
		return null;
	}

	/**
	 * @param element to get foreground color for.
	 * @return RGB to use as foreground color.  If <code>null</code> use default color
	 */
	protected RGB getForegroundForRoster(Roster element) {
		return null;
	}

	/**
	 * @param element to get fontdata for.  This implementation returns
	 * null, meaning that the default font will be used.  Subclasses should override as appropriate.
	 * @return FontData to use for rendering given element.  If <code>null</code> use default FontData
	 */
	protected FontData getFontForRoster(Roster element) {
		return null;
	}

	/**
	 * @param element to get background color for.  This implementation returns
	 * null, meaning that the default color will be used.  Subclasses should override as appropriate.
	 * @return RGB to use as background color.  If <code>null</code> use default color
	 */
	protected RGB getBackgroundForRoster(Roster element) {
		return null;
	}

	public Class[] getAdapterList() {
		return new Class[] { IWorkbenchAdapter2.class };
	}

}
