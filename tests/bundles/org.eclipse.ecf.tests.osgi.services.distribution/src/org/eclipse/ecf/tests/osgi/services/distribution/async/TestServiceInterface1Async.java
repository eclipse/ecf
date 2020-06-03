/****************************************************************************
 * Copyright (c) 2011 Composent and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Scott Lewis - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.osgi.services.distribution.async;

import org.eclipse.equinox.concurrent.future.IFuture;

public interface TestServiceInterface1Async {

	IFuture doStuff1();
	
}
