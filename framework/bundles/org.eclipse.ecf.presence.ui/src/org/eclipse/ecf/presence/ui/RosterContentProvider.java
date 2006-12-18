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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ecf.presence.roster.IRoster;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class RosterContentProvider implements IRosterContentProvider {

	protected List rosters = new ArrayList();

	private Object input = null;

	protected void setInput(Object obj) {
		this.input = obj;
	}

	protected Object getInput() {
		return this.input;
	}

	protected IWorkbenchAdapter getAdapter(Object element) {
		IWorkbenchAdapter adapter = null;
		if (element instanceof IAdaptable)
			adapter = (IWorkbenchAdapter) ((IAdaptable) element)
					.getAdapter(IWorkbenchAdapter.class);
		if (element != null && adapter == null)
			adapter = (IWorkbenchAdapter) Platform.getAdapterManager()
					.loadAdapter(element, IWorkbenchAdapter.class.getName());
		return adapter;
	}

	protected Object[] getRootChildren() {
		return rosters.toArray();
	}
	
	public Object[] getChildren(Object parentElement) {
		if (parentElement == getInput()) return getRootChildren();
		
		IWorkbenchAdapter adapter = getAdapter(parentElement);
		if (adapter != null)
			return adapter.getChildren(parentElement);
		return new Object[0];
	}

	protected Object getRosterParent(IRoster roster) {
		return getInput();
	}
	
	public Object getParent(Object element) {
		if (element == getInput()) return null;
		if (element instanceof IRoster) return getRosterParent(((IRoster) element));
		
		IWorkbenchAdapter adapter = getAdapter(element);
		if (adapter != null)
			return adapter.getParent(element);
		return null;
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
		if (rosters != null) {
			rosters.clear();
			rosters = null;
		}
		setInput(null);
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		setInput(newInput);
	}

	public boolean add(IRoster roster) {
		if (roster == null)
			return false;
		return rosters.add(roster);
	}

	public boolean remove(IRoster roster) {
		if (roster == null)
			return false;
		return rosters.remove(roster);
	}

}
