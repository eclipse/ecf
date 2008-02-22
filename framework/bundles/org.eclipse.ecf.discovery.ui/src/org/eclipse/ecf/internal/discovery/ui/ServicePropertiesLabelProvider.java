/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.internal.discovery.ui;

import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;

public class ServicePropertiesLabelProvider extends LabelProvider {

	public String getText(Object element) {
		if (element instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) element;
			Object selected = ss.getFirstElement();
			if (selected instanceof ViewTreeService) {
				ViewTreeService treeParent = (ViewTreeService) selected;
				if (treeParent.getID() != null) {
					IServiceInfo serviceInfo = treeParent.getServiceInfo();
					if (serviceInfo != null)
						return serviceInfo.getServiceID().getServiceName();
				}
			}
		}
		return null;
	}

}
