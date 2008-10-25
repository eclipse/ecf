/*******************************************************************************
 * Copyright (c) 2008 Versant Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Kuppe (mkuppe <at> versant <dot> com) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.tests.provider.discovery;

import org.eclipse.ecf.tests.discovery.DiscoveryTest;

public class CompositeDiscoveryContainerTest extends DiscoveryTest {

	public CompositeDiscoveryContainerTest() {
		super("ecf.discovery.*");
		setComparator(new CompositeServiceInfoComporator());
	}
}
