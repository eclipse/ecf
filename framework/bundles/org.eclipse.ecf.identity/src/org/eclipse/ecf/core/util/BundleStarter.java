/*******************************************************************************
 * Copyright (c) 2018 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.util;

import java.util.Arrays;
import java.util.List;
import org.osgi.framework.*;

/**
 * @since 3.9
 */
public class BundleStarter {

	public static void startDependents(BundleContext context, String[] bundleSymbolicNames, int stateMask)
			throws BundleException {
		List<String> bsns = Arrays.asList(bundleSymbolicNames);
		for (Bundle b : context.getBundles())
			if (bsns.contains(b.getSymbolicName()) && ((b.getState() & stateMask) != 0))
				b.start();
	}
}
