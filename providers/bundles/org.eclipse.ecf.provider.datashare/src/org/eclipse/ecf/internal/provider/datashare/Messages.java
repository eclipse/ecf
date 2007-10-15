/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.internal.provider.datashare;

import org.eclipse.osgi.util.NLS;

/**
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ecf.internal.provider.datashare.messages"; //$NON-NLS-1$
	public static String BaseChannel_EXCEPTION_BAD_RECEIVER_ID;
	public static String BaseChannel_EXCEPTION_CHANNEL_CONTAINER_NULL;
	public static String DatashareContainerAdapter_EXCEPTION_CREATING_ADAPTER;
	public static String SharedObjectDatashareContainerAdapter_EXCEPTION_CANNOT_CREATE_OBJECT_OF_TYPE;
	public static String SharedObjectDatashareContainerAdapter_EXCEPTION_CANNOTCREATESHAREDOBJECT;
	public static String BaseChannel_EXCEPTION_RECEIVER_NULL;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		//
	}
}
