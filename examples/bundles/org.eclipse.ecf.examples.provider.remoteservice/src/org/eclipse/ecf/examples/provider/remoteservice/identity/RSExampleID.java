/****************************************************************************
 * Copyright (c) 2011 Composent, Inc. and others.
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
package org.eclipse.ecf.examples.provider.remoteservice.identity;

import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.identity.StringID;

public class RSExampleID extends StringID {

	private static final long serialVersionUID = -3656862045346223983L;

	protected RSExampleID(Namespace n, String s) {
		super(n, s);
	}

}
