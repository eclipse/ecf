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

package org.eclipse.ecf.internal.core.sharedobject;

import org.eclipse.osgi.util.NLS;

/**
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ecf.internal.core.sharedobject.messages"; //$NON-NLS-1$
	public static String AbstractSharedObjectContainerAdapterFactory_Exception_Adding_Adapter;
	public static String Activator_Exception_Removing_Extension;
	public static String BaseSharedObject_Message_Not_Null;
	public static String SharedObjectContainerFactory_Exception_Container_Wrong_Type;
	public static String SharedObjectContainerFactory_Exception_Description_Not_Null;
	public static String SharedObjectFactory_Description_Not_Null;
	//public static String SharedObjectFactory_Exception_Create_Instantiator;
	//public static String SharedObjectFactory_Exception_Create_Instantiator_Null;
	public static String SharedObjectFactory_Exception_Create_Instantiator_X_Null;
	//public static String SharedObjectFactory_Exception_Create_Shared_Objec;
	//public static String SharedObjectFactory_Exception_Create_Shared_Object;
	//public static String SharedObjectFactory_Exception_Create_Shared_Object_Not_Found;
	public static String SharedObjectFactory_SharedObjectCreateException_X_Not_Found;
	public static String SharedObjectFactory_SharedObjectDescription_X_Not_Found;
	public static String SharedObjectFactory_Exception_Create_With_Description;
	public static String SharedObjectMsg_Exception_Methodname_Not_Null;
	public static String SharedObjectMsg_Exception_Not_Serializable;
	public static String SharedObjectMsg_Exception_Null_Target;
	public static String SharedObjectMsg_Exception_Param;
	public static String SharedObjectMsg_Excepton_Invalid_Shared_Object_Msg;
	public static String TwoPhaseCommitEventProcessor_Exception_Commit_Timeout;
	public static String TwoPhaseCommitEventProcessor_Exception_Shared_Object_Add_Abort;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// private null constructor
	}
}
