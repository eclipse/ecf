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

import org.eclipse.team.core.variants.IResourceVariant;

public final class FetchResponse implements IResponse {

	private static final long serialVersionUID = -3291936095528478752L;

	private final IResourceVariant[] variants;

	public FetchResponse() {
		this(new IResourceVariant[0]);
	}

	public FetchResponse(IResourceVariant variant) {
		this(new IResourceVariant[] {variant});
	}

	public FetchResponse(IResourceVariant[] variants) {
		this.variants = variants;
	}

	public Object getResponse() {
		return variants;
	}

}
