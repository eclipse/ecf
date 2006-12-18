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

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class RosterView extends ViewPart {

	protected TreeViewer treeViewer;
	
	protected RosterLabelProvider rosterLabelProvider;
	protected RosterContentProvider rosterContentProvider;
	
	public RosterView() {
	}
	
	public void createPartControl(Composite parent) {
		treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		getSite().setSelectionProvider(treeViewer);
		rosterContentProvider = new RosterContentProvider();
		rosterLabelProvider = new RosterLabelProvider();
		treeViewer.setLabelProvider(rosterLabelProvider);
		treeViewer.setContentProvider(rosterContentProvider);
		treeViewer.setInput(new Object());
	}

	public void setFocus() {
		treeViewer.getControl().setFocus();
	}

}
