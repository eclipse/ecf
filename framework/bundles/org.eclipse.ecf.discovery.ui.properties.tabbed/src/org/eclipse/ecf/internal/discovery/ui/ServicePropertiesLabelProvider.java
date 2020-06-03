/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *    Markus Alexander Kuppe - https://bugs.eclipse.org/256603
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.discovery.ui;

import org.eclipse.ecf.discovery.ui.model.IServiceInfo;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;

public class ServicePropertiesLabelProvider extends LabelProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (element instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) element;
			Object selected = ss.getFirstElement();
			if (selected instanceof IServiceInfo) {
				IServiceInfo si = (IServiceInfo) selected;
				return si.getServiceID().getEcfServiceName();
			}
		}
		return null;
	}

}
