/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core;

import java.util.Dictionary;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * Interaction with local OSGI services via ECF plugin. Provided to
 * ISharedObject instances via ISharedObjectContext
 * 
 */
public interface IOSGIService {
	// OSGI Service interfaces
	public ServiceReference getServiceReference(String svc);

	public Object getService(ServiceReference reference);

	public ServiceReference[] getServiceReferences(String clazz, String filter)
			throws InvalidSyntaxException;

	public ServiceRegistration registerService(String[] clazzes,
			Object service, Dictionary properties);

	public ServiceRegistration registerService(String clazz, Object service,
			Dictionary properties);
}