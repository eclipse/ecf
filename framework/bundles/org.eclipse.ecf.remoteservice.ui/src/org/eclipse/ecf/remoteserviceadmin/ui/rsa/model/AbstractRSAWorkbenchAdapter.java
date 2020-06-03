/****************************************************************************
 * Copyright (c) 2015 Composent, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Scott Lewis - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.remoteserviceadmin.ui.rsa.model;

import org.eclipse.ui.model.WorkbenchAdapter;

/**
 * @since 3.3
 */
public class AbstractRSAWorkbenchAdapter extends WorkbenchAdapter {

	@Override
	public Object getParent(Object object) {
		return ((AbstractRSANode) object).getParent();
	}

	@Override
	public Object[] getChildren(Object object) {
		return ((AbstractRSANode) object).getChildren();
	}

}
