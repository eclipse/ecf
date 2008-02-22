package org.eclipse.ecf.discovery.ui.views;

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
