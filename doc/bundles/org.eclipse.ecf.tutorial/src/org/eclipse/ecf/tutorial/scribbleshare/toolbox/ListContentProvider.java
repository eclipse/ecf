/****************************************************************************
 * Copyright (c) 2006 IBM, Inc and Composent, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Ken Gilmer <kgilmer@gmail.com> - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.tutorial.scribbleshare.toolbox;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Palette consists of a List of AbstractTools.
 * @author kgilmer
 *
 */
public class ListContentProvider implements IStructuredContentProvider {

	public Object[] getElements(Object inputElement) {
		
		return ((List)inputElement).toArray();
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
