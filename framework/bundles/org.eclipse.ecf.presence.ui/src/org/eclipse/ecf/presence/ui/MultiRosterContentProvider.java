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
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ecf.presence.roster.IRoster;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Content provider for multiple roster viewer. This content provider implements
 * an IMultiRosterContentProvider suitable for use by tree viewers that accepts
 * ITreeContentProviders as input. This class may be subclassed in order to
 * customize the behavior/display of other content providers.
 * 
 */
public class MultiRosterContentProvider implements IMultiRosterContentProvider {

	protected List rosters = Collections.synchronizedList(new ArrayList());

	private Object root;
	private Object invisibleRoot;

	private IViewPart viewer = null;

	protected void setViewer(IViewPart viewer) {
		this.viewer = viewer;
	}

	protected IViewPart getViewer() {
		return viewer;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		if (parentElement.equals(root))
			return getRootChildren();
		if (parentElement.equals(invisibleRoot))
			return new Object[] { root };

		IWorkbenchAdapter adapter = getAdapter(parentElement);
		if (adapter != null)
			return adapter.getChildren(parentElement);
		return new Object[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		if (element.equals(invisibleRoot))
			return null;
		if (element.equals(root))
			return invisibleRoot;
		if (element instanceof IRoster)
			return root;

		IWorkbenchAdapter adapter = getAdapter(element);
		if (adapter != null)
			return adapter.getParent(element);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IViewPart
				&& inputElement.equals(getViewer().getViewSite()))
			return getChildren(root);
		return getChildren(inputElement);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		if (rosters != null) {
			rosters.clear();
			rosters = null;
		}
		setViewer(null);
		root = null;
		invisibleRoot = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (viewer instanceof IViewPart)
			setViewer((IViewPart) viewer);
		root = newInput;
		invisibleRoot = "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.presence.ui.IMultiRosterContentProvider#add(org.eclipse.ecf.presence.roster.IRoster)
	 */
	public boolean add(IRoster roster) {
		if (roster == null)
			return false;
		return rosters.add(roster);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.presence.ui.IMultiRosterContentProvider#remove(org.eclipse.ecf.presence.roster.IRoster)
	 */
	public boolean remove(IRoster roster) {
		if (roster == null)
			return false;
		return rosters.remove(roster);
	}

}
