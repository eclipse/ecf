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

package org.eclipse.ecf.internal.examples.remoteservices.server;

import org.eclipse.ecf.examples.remoteservices.common.IConcatService;

/**
 *
 */
public class ConcatServiceImpl implements IConcatService {

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.examples.remoteservices.common.IConcatService#concat(java.lang.String, java.lang.String)
	 */
	public String concat(String first, String second) {
		return first.concat(second);
	}

}
