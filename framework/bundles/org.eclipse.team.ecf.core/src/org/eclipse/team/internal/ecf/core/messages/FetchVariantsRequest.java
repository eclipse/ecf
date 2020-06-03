/****************************************************************************
 * Copyright (c) 2008 Versant Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Remy Chi Jian Suen (Versant Corporation) - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.team.internal.ecf.core.messages;

import org.eclipse.ecf.core.identity.ID;

public class FetchVariantsRequest extends FetchVariantRequest {

	private static final long serialVersionUID = -5776703885952265394L;

	public FetchVariantsRequest(ID fromId, String path, int type) {
		super(fromId, path, type);
	}

}
