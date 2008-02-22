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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySourceProvider;
import org.eclipse.ui.views.properties.tabbed.AdvancedPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

public class ServicePropertiesPropertySection extends AdvancedPropertySection {

	class PropertySourceProvider implements IPropertySourceProvider {

		/* (non-Javadoc)
		 * @see org.eclipse.ui.views.properties.IPropertySourceProvider#getPropertySource(java.lang.Object)
		 */
		public IPropertySource getPropertySource(Object object) {
			if (object instanceof ViewTreeService) {
				ViewTreeService treeParent = (ViewTreeService) object;
				if (treeParent.getID() != null)
					return new ServicePropertiesPropertySource(treeParent.getServiceInfo().getServiceProperties());
			}
			return null;
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AdvancedPropertySection#createControls(org.eclipse.swt.widgets.Composite, org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	public void createControls(Composite parent, TabbedPropertySheetPage tabbedPropertySheetPage) {
		super.createControls(parent, tabbedPropertySheetPage);
		page.setPropertySourceProvider(new PropertySourceProvider());
	}
}
