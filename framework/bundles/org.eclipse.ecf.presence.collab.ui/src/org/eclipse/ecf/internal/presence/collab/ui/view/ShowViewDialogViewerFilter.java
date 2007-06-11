package org.eclipse.ecf.internal.presence.collab.ui.view;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.views.IViewDescriptor;

/**
 *
 */
public class ShowViewDialogViewerFilter extends ViewerFilter {
	public boolean select(Viewer viewer, Object parentElement,
			Object element) {
		if (element instanceof IViewDescriptor
				&& "org.eclipse.ui.internal.introview" //$NON-NLS-1$
						.equals(((IViewDescriptor) element)
								.getId()))
			return false;
		else
			return true;
	}
}