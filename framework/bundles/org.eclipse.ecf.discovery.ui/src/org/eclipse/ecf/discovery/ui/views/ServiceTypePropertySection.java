package org.eclipse.ecf.discovery.ui.views;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.*;

public class ServiceTypePropertySection extends AbstractPropertySection {

	private Text location;
	private Text serviceTypeID;
	private Text serviceTypeIDInternal;
	private Text serviceTypeIDNamespace;

	private IServiceInfo serviceInfo;

	public ServiceTypePropertySection() {
		// nothing
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#createControls(org.eclipse.swt.widgets.Composite, org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	public void createControls(Composite parent, TabbedPropertySheetPage tabbedPropertySheetPage) {
		super.createControls(parent, tabbedPropertySheetPage);
		Composite composite = getWidgetFactory().createFlatFormComposite(parent);
		FormData data;

		// Location
		location = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, 0);
		location.setLayoutData(data);
		CLabel labelLabel = getWidgetFactory().createCLabel(composite, "Location:"); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(location, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(location, 0, SWT.CENTER);
		labelLabel.setLayoutData(data);

		// ServiceTypeID
		serviceTypeID = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(location, 0);
		serviceTypeID.setLayoutData(data);
		labelLabel = getWidgetFactory().createCLabel(composite, "TypeID:"); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(serviceTypeID, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(serviceTypeID, 0, SWT.CENTER);
		labelLabel.setLayoutData(data);

		// ServiceTypeID internal
		serviceTypeIDInternal = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(serviceTypeID, 0);
		serviceTypeIDInternal.setLayoutData(data);
		labelLabel = getWidgetFactory().createCLabel(composite, "Internal:"); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(serviceTypeIDInternal, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(serviceTypeIDInternal, 0, SWT.CENTER);
		labelLabel.setLayoutData(data);

		// ServiceTypeID namespace
		serviceTypeIDNamespace = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(serviceTypeIDInternal, 0);
		serviceTypeIDNamespace.setLayoutData(data);
		labelLabel = getWidgetFactory().createCLabel(composite, "Namespace:"); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(serviceTypeIDNamespace, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(serviceTypeIDNamespace, 0, SWT.CENTER);
		labelLabel.setLayoutData(data);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#dispose()
	 */
	public void dispose() {
		super.dispose();
		serviceInfo = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#setInput(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		Assert.isTrue(selection instanceof IStructuredSelection);
		Object input = ((IStructuredSelection) selection).getFirstElement();
		Assert.isTrue(input instanceof ViewTreeService);
		ViewTreeService parent = (ViewTreeService) input;
		if (parent.getID() != null)
			serviceInfo = parent.getServiceInfo();
		else
			serviceInfo = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#refresh()
	 */
	public void refresh() {
		if (serviceInfo != null) {
			location.setText(serviceInfo.getLocation().toString());
			location.setEditable(false);
			serviceTypeID.setText(serviceInfo.getServiceID().getServiceTypeID().getName());
			serviceTypeID.setEditable(false);
			serviceTypeIDInternal.setText(serviceInfo.getServiceID().getServiceTypeID().getInternal());
			serviceTypeIDInternal.setEditable(false);
			serviceTypeIDNamespace.setText(serviceInfo.getServiceID().getServiceTypeID().getNamespace().getName());
			serviceTypeIDNamespace.setEditable(false);
		}
	}
}
