/****************************************************************************
 * Copyright (c) 2009 Markus Alexander Kuppe.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Markus Alexander Kuppe (ecf-dev_eclipse.org <at> lemmster <dot> de) - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.provider.jslp;

import org.eclipse.ecf.provider.jslp.container.JSLPDiscoveryContainer;
import org.eclipse.ecf.tests.discovery.DiscoveryTestsWithoutRegister;

public class JSLPDiscoveryWithoutRegTest extends DiscoveryTestsWithoutRegister {

	public JSLPDiscoveryWithoutRegTest() {
		super(JSLPDiscoveryContainer.NAME);
	}

}
