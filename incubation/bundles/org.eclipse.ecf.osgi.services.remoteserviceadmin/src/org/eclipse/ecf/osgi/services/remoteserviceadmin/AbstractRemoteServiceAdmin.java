/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import org.eclipse.core.runtime.IStatus;


public abstract class AbstractRemoteServiceAdmin {

	protected void logError(String method, String message, IStatus result) {
		// TODO Auto-generated method stub
		logError(method,method);
		
	}

	protected void trace(String method, String message) {
		// TODO Auto-generated method stub
		System.out.println("TopologyManager." + method + ": " + message);
	}

	protected void logWarning(String string) {
		System.out.println(string);
	}

	protected void logError(String method, String message) {
		// TODO Auto-generated method stub
		
	}


}
