/****************************************************************************
 * Copyright (c) 2009 Jan S. Rellermeyer and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jan S. Rellermeyer - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.tests.osgi.services.distribution;

import org.eclipse.ecf.osgi.services.distribution.IDistributionConstants;
import org.eclipse.ecf.tests.ECFAbstractTestCase;
import org.eclipse.ecf.tests.internal.osgi.services.distribution.Activator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.distribution.DistributionProvider;

public class DistributionProviderTest extends ECFAbstractTestCase implements IDistributionConstants {

	public void testDistributionProvider() throws Exception {
		final BundleContext context = Activator.getDefault().getContext();
		assertTrue(context != null);

		final ServiceReference ref = context
				.getServiceReference(DistributionProvider.class.getName());
		assertTrue(ref != null);
		assertTrue(ref.getProperty(DistributionProvider.PRODUCT_NAME) != null);
		assertTrue(ref.getProperty(DistributionProvider.PRODUCT_VERSION) != null);
		assertTrue(ref.getProperty(DistributionProvider.VENDOR_NAME) != null);
	}

}
