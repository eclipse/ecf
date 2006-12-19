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

import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ecf.core.user.IUser;
import org.eclipse.ecf.presence.roster.IPresence;
import org.eclipse.ecf.presence.roster.Roster;
import org.eclipse.ecf.presence.roster.RosterEntry;
import org.eclipse.ecf.presence.roster.RosterGroup;
import org.eclipse.ecf.presence.roster.RosterItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class RosterAdapterFactory implements IAdapterFactory {

	private IWorkbenchAdapter rosterAdapter = new IWorkbenchAdapter() {

		public Object[] getChildren(Object o) {
			Roster roster = (Roster) o;
			return roster.getItems().toArray();
		}

		public ImageDescriptor getImageDescriptor(Object object) {
			return null;
		}

		public String getLabel(Object o) {
			Roster roster = (Roster) o;
			IUser user = roster.getUser();
			if (user == null) return "(disconnected)";
			else return user.getName();
		}

		public Object getParent(Object o) {
			return null;
		}};
	
	private IWorkbenchAdapter rosterGroupAdapter = new IWorkbenchAdapter() {

		public Object[] getChildren(Object o) {
			return ((RosterGroup) o).getEntries().toArray();
		}

		public ImageDescriptor getImageDescriptor(Object object) {
			return null;
		}

		public String getLabel(Object o) {
			return ((RosterGroup) o).getName();
		}

		public Object getParent(Object o) {
			return ((RosterGroup) o).getParent();
		}
		
	};

	private IWorkbenchAdapter rosterItemAdapter = new IWorkbenchAdapter() {

		public Object[] getChildren(Object o) {
			return new Object[0];
		}

		public ImageDescriptor getImageDescriptor(Object object) {
			return null;
		}

		public String getLabel(Object o) {
			return ((RosterItem) o).getName();
		}

		public Object getParent(Object o) {
			return ((RosterItem) o).getParent();
		}
		
	};

	protected Object [] getRosterEntryChildrenFromPresence(RosterEntry entry) {
		IPresence presence = entry.getPresence();
		Map properties = presence.getProperties();
		Object [] children = new Object[4+properties.size()];
		children[0] = new RosterItem(entry, "User: "+entry.getUser().getName());
		children[1] = new RosterItem(entry, "Mode: "+presence.getMode().toString());
		children[2] = new RosterItem(entry, "Type: "+presence.getType().toString());
		children[3] = new RosterItem(entry, "Status: "+presence.getStatus());
		int index = 4;
		for(Iterator i=properties.keySet().iterator(); i.hasNext(); index++) {
			children[index] = properties.get(i.next());
		}
		return children;
	}
	
	private IWorkbenchAdapter rosterEntryAdapter = new IWorkbenchAdapter() {

		public Object[] getChildren(Object o) {
			return getRosterEntryChildrenFromPresence((RosterEntry) o);
		}

		public ImageDescriptor getImageDescriptor(Object object) {
			return null;
		}

		public String getLabel(Object o) {
			return ((RosterEntry) o).getName();
		}

		public Object getParent(Object o) {
			return ((RosterEntry) o).getParent();
		}
		
	};

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType.equals(IWorkbenchAdapter.class)) {
			if (adaptableObject instanceof Roster) return rosterAdapter;
			if (adaptableObject instanceof RosterGroup) return rosterGroupAdapter;
			if (adaptableObject instanceof RosterEntry) return rosterEntryAdapter;
			if (adaptableObject instanceof RosterItem) return rosterItemAdapter;
		}
		return null;
	}

	public Class[] getAdapterList() {
		return new Class[] { IWorkbenchAdapter.class };
	}

}
