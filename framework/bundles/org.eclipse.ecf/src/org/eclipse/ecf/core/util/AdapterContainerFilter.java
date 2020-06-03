/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
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

package org.eclipse.ecf.core.util;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.IContainer;

/**
 *
 */
public class AdapterContainerFilter implements IContainerFilter {

	private Class clazz;
	private Object result;

	public AdapterContainerFilter(Class clazz) {
		Assert.isNotNull(clazz);
		this.clazz = clazz;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.util.IContainerFilter#match(org.eclipse.ecf.core.IContainer)
	 */
	public boolean match(IContainer containerToMatch) {
		result = containerToMatch.getAdapter(clazz);
		return result != null;
	}

	public Object getMatchResult() {
		return result;
	}
}
