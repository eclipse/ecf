/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.presence.collab.ui.view;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.views.IViewDescriptor;

public class ShowViewDialogViewerFilter extends ViewerFilter {
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof IViewDescriptor && "org.eclipse.ui.internal.introview" //$NON-NLS-1$
				.equals(((IViewDescriptor) element).getId()))
			return false;
		return true;
	}
}