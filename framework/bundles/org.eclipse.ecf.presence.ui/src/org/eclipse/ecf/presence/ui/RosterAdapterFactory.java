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
import org.eclipse.ecf.presence.roster.IPresence;
import org.eclipse.ecf.presence.roster.IRoster;
import org.eclipse.ecf.presence.roster.IRosterEntry;
import org.eclipse.ecf.presence.roster.IRosterGroup;
import org.eclipse.ecf.presence.roster.Roster;
import org.eclipse.ecf.presence.roster.RosterEntry;
import org.eclipse.ecf.presence.roster.RosterGroup;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class RosterAdapterFactory implements IAdapterFactory {

	private IWorkbenchAdapter rosterAdapter = new IWorkbenchAdapter() {

		public Object[] getChildren(Object o) {
			IRoster roster = (IRoster) o;
			return roster.getItems().toArray();
		}

		public ImageDescriptor getImageDescriptor(Object object) {
			return null;
		}

		public String getLabel(Object o) {
			IRoster roster = (IRoster) o;
			return roster.getUser().getName();
		}

		public Object getParent(Object o) {
			return null;
		}};
	
	private IWorkbenchAdapter rosterGroupAdapter = new IWorkbenchAdapter() {

		public Object[] getChildren(Object o) {
			return ((IRosterGroup) o).getEntries().toArray();
		}

		public ImageDescriptor getImageDescriptor(Object object) {
			return null;
		}

		public String getLabel(Object o) {
			return ((IRosterGroup) o).getName();
		}

		public Object getParent(Object o) {
			return ((IRosterGroup) o).getParent();
		}
		
	};

	private IWorkbenchAdapter rosterEntryAdapter = new IWorkbenchAdapter() {

		public Object[] getChildren(Object o) {
			IRosterEntry entry = (IRosterEntry) o;
			IPresence presence = entry.getPresence();
			String [] children = new String[4];
			// XXX testing
			children[0] = "Mode: "+presence.getMode().toString();
			children[1] = "Type: "+presence.getType().toString();
			children[2] = "Status: "+presence.getStatus();
			children[3] = "User: "+entry.getUser().getName();
			children[4] = "ID: "+entry.getUser().getID().getName();
			return children;
		}

		public ImageDescriptor getImageDescriptor(Object object) {
			return null;
		}

		public String getLabel(Object o) {
			return ((IRosterEntry) o).getName();
		}

		public Object getParent(Object o) {
			return ((IRosterGroup) o).getParent();
		}
		
	};

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType.equals(IWorkbenchAdapter.class)) {
			if (adaptableObject instanceof Roster) return rosterAdapter;
			if (adaptableObject instanceof RosterGroup) return rosterGroupAdapter;
			if (adaptableObject instanceof RosterEntry) return rosterEntryAdapter;
		}
		return null;
	}

	public Class[] getAdapterList() {
		return new Class[] { IWorkbenchAdapter.class };
	}

}
