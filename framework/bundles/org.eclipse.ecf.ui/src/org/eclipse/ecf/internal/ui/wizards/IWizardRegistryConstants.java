/****************************************************************************
 * Copyright (c) 2004, 2007 Composent, Inc. and others.
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

package org.eclipse.ecf.internal.ui.wizards;

import org.eclipse.ecf.internal.ui.Activator;

public interface IWizardRegistryConstants {

	public static final String ATT_ID = "id"; //$NON-NLS-1$

	public static final String ATT_NAME = "name"; //$NON-NLS-1$

	public static final String ATT_CLASS = "class"; //$NON-NLS-1$

	public static final String ATT_DESCRIPTION = "description"; //$NON-NLS-1$

	public static final String ATT_ICON = "icon"; //$NON-NLS-1$

	public static final String ATT_DESCRIPTION_IMAGE = "descriptionImage"; //$NON-NLS-1$

	public static final String ELEMENT_WIZARD = "wizard"; //$NON-NLS-1$

	public static final String ELEMENT_CATEGORY = "category"; //$NON-NLS-1$

	public static final String ATT_PARENT_CATEGORY = "parentCategory"; //$NON-NLS-1$

	public static final String CONFIGURE_EPOINT = "configurationWizards"; //$NON-NLS-1$

	public static final String CONNECT_EPOINT = "connectWizards"; //$NON-NLS-1$

	public static final String CONFIGURE_EPOINT_ID = Activator.PLUGIN_ID + '.'
			+ CONFIGURE_EPOINT;

	public static final String CONNECT_EPOINT_ID = Activator.PLUGIN_ID + '.'
			+ CONNECT_EPOINT;

	public static final String PRIMARY_WIZARD = "primaryWizard"; //$NON-NLS-1$

	public static final String HAS_PAGES = "hasPages"; //$NON-NLS-1$

	public static final String CAN_FINISH_EARLY = "canFinishEarly"; //$NON-NLS-1$

	public static final String HELP_HREF = "helpHref"; //$NON-NLS-1$

	public static final String ATT_CONTAINER_TYPE_NAME = "containerFactoryName"; //$NON-NLS-1$

}
