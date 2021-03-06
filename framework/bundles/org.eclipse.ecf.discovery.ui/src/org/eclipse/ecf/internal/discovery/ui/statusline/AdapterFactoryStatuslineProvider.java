/****************************************************************************
 * Copyright (c) 2008 Versant Corp. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Versant Corp. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.discovery.ui.statusline;

import org.eclipse.ecf.discovery.ui.model.IItemStatusLineProvider;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.*;

public class AdapterFactoryStatuslineProvider implements ISelectionChangedListener {
	private ComposedAdapterFactory adapterFactory;
	private IStatusLineManager statusline;

	/**
	 * @param aStatusline
	 * @param adapterFactory
	 */
	public AdapterFactoryStatuslineProvider(ComposedAdapterFactory anAdapterFactory, IStatusLineManager aStatusline) {
		adapterFactory = anAdapterFactory;
		statusline = aStatusline;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			EObject object = (EObject) ss.getFirstElement();
			if (object != null) { // do we really have a selection?
				IItemStatusLineProvider itemStatusLineProvider = (IItemStatusLineProvider) adapterFactory.adapt(object, IItemStatusLineProvider.class);
				if (itemStatusLineProvider != null) {
					statusline.setMessage(itemStatusLineProvider.getStatusLineText(object));
				} else {
					// fallback to IItemLabelProvider.getText(..)
					IItemLabelProvider itemLabelProvider = (IItemLabelProvider) adapterFactory.adapt(object, IItemLabelProvider.class);
					if (itemLabelProvider != null) {
						statusline.setMessage(itemLabelProvider.getText(object));
					}
				}

			}
		} else {
			statusline.setMessage(""); //$NON-NLS-1$
		}
	}
}
