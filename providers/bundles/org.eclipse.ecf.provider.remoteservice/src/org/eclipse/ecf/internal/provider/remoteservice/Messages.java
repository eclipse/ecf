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

package org.eclipse.ecf.internal.provider.remoteservice;

import org.eclipse.osgi.util.NLS;

/**
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ecf.internal.provider.remoteservice.messages"; //$NON-NLS-1$
	public static String RegistrySharedObject_15;
	public static String RegistrySharedObject_19;
	public static String RegistrySharedObject_7;
	public static String RegistrySharedObject_EXCEPTION_IN_REMOTE_CALL;
	public static String RemoteFilter_EXCEPTION_FILTER_NOT_NULL;
	public static String RemoteServiceImpl_EXCEPTION_CREATING_PROXY;
	public static String RegistrySharedObject_EXCEPTION_INVALID_RESPONSE;
	public static String RemoteServiceRegistrationImpl_EXCEPTION_SERVICE_ALREADY_REGISTERED;
	public static String RegistrySharedObject_EXCEPTION_REQUEST_NOT_FOUND;
	public static String RegistrySharedObject_EXCEPTION_SENDING_ADD_SERVICE;
	public static String RegistrySharedObject_EXCEPTION_SENDING_CALL_REQUEST;
	public static String RegistrySharedObject_EXCEPTION_SENDING_FIRE_REQUEST;
	public static String RegistrySharedObject_EXCEPTION_SENDING_REMOTE_REQUEST;
	public static String RegistrySharedObject_EXCEPTION_SENDING_REQUEST;
	public static String RegistrySharedObject_EXCEPTION_SENDING_RESPONSE;
	public static String RegistrySharedObject_EXCEPTION_SENDING_SERVICE_UNREGISTER;
	public static String RegistrySharedObject_EXCEPTION_SERVICE_CANNOT_BE_NULL;
	public static String RegistrySharedObject_EXCEPTION_SERVICE_CLASSES_LIST_EMPTY;
	public static String RegistrySharedObject_EXCEPTION_SHARED_OBJECT_INVOKE;
	public static String RegistrySharedObject_EXCEPTION_TIMEOUT_FOR_CALL_REQUEST;
	public static String RegistrySharedObject_EXCEPTION_WAIT_INTERRUPTED;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		//
	}
}
