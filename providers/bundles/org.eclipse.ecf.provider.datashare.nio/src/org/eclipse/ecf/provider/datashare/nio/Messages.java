/******************************************************************************
 * Copyright (c) 2009 Remy Chi Jian Suen and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Remy Chi Jian Suen - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.datashare.nio;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.ecf.provider.datashare.nio.messages"; //$NON-NLS-1$

	public static String NIOChannel_CouldNotCreateServerSocket;
	public static String NIOChannel_BindOperationFailed;
	public static String NIOChannel_ReceiverUnspecified;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

}
