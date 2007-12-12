/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.internal.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ecf.internal.core.messages"; //$NON-NLS-1$
	public static String AbstractContainer_Exception_Callback_Handler;
	public static String BooleanCallback_EXCEPTION_INVALID_BOOLEAN_ARGUMENT;
	public static String ContainerFactory_Base_Container_Name;
	public static String ContainerFactory_Exception_Adapter_Not_Null;
	public static String ContainerFactory_EXCEPTION_CONTAINER_ID_NOT_NULL;
	public static String ContainerFactory_Exception_Create_Container;
	public static String ContainerFactory_Exception_Platform_Running;
	public static String ContainerFactory_Warning_Running_Standalone;
	public static String ContainerTypeDescription_Instantiator_Class_Not_Null;
	public static String ContainerTypeDescription_Instantiator_Instance_Not_Null;
	public static String ContainerTypeDescription_Name_Not_Null;
	public static String ECFPlugin_Container_Name_Collision_Prefix;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// private null constructor
	}
}
