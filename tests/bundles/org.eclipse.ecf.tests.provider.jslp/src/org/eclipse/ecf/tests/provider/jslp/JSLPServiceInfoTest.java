/****************************************************************************
 * Copyright (c) 2007 Versant Corp.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Markus Kuppe (mkuppe <at> versant <dot> com) - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.provider.jslp;

import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.provider.jslp.identity.JSLPNamespace;
import org.eclipse.ecf.tests.discovery.ServiceInfoTest;

public class JSLPServiceInfoTest extends ServiceInfoTest {

	public JSLPServiceInfoTest() {
		super(IDFactory.getDefault().getNamespaceByName(JSLPNamespace.NAME));
	}
}
