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

package org.eclipse.ecf.internal.provider;

import org.eclipse.osgi.util.NLS;

/**
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ecf.internal.provider.messages"; //$NON-NLS-1$
	public static String Client_Already_Connected;
	public static String Client_Class_Load_Failure_Protocol_Violation;
	public static String Client_Event_Handler_Not_Null;
	public static String Client_Exception_Not_Connected;
	public static String Client_Invalid_Message;
	public static String Client_Invalid_URI;
	public static String Client_Remote_No_Ping;
	public static String ClientSOContainer_Already_Connected;
	public static String ClientSOContainer_Connect_Failed_Incorrect_State;
	public static String ClientSOContainer_Container_Closing;
	public static String ClientSOContainer_Currently_Connecting;
	public static String ClientSOContainer_Exception_ID_Array_Null;
	public static String ClientSOContainer_EXCEPTION_TARGETID_NOT_NULL;
	public static String ClientSOContainer_Invalid_Server_Response;
	public static String ClientSOContainer_Is_Not_Same;
	public static String ClientSOContainer_Not_Connected;
	public static String ClientSOContainer_ServerID_Cannot_Be_Null;
	public static String ClientSOContainer_View_Change_Is_Null;
	public static String ClientSOContainer_View_Change_Message;
	public static String ExObjectInputStream_Exception_Could_Not_Setup_Object_Replacers;
	public static String ExObjectOutputStream_Could_Not_Setup_Object_Replacers;
	public static String Server_Listener_Not_Null;
	public static String ServerSOContainer_Connect_Request_Null;
	public static String ServerSOContainer_Exception_Server_Refused;
	public static String ServerSOContainer_FromID_Null;
	public static String ServerSOContainer_Server_Application_Cannot_Connect;
	public static String ServerSOContainer_Server_Closing;
	public static String SOContainer_Exception_Add_Object;
	public static String SOContainer_Exception_Already_Exists_In_Container;
	public static String SOContainer_Exception_Already_In_Container;
	public static String SOContainer_Exception_Bad_Container_Message;
	public static String SOContainer_Exception_Bad_Description;
	public static String SOContainer_EXCEPTION_CLASS_NOT_FOUND;
	public static String SOContainer_EXCEPTION_NOT_CONTAINER_MESSAGE;
	public static String SOContainer_EXCEPTION_INVALID_CLASS;
	public static String SOContainer_Exception_Config_Not_Null;
	public static String SOContainer_Exception_Object_With_ID;
	public static String SOContainer_Exception_ObjectID_Is_Null;
	public static String SOContainer_Loading_Interrupted;
	public static String SOContainer_Not_Serializable;
	public static String SOContainer_Rejected_By_Container;
	public static String SOContainer_Shared_Object;
	public static String SOContainer_Shared_Object_Message;
	public static String SOManager_Connector;
	public static String SOManager_Container;
	public static String SOManager_Does_Not_Implement;
	public static String SOManager_Exception_Adding_Shared_Object;
	public static String SOManager_Exception_Connector_Not_Null;
	public static String SOManager_Exception_Creating_Shared_Object;
	public static String SOManager_Exception_Receivers_Not_Null;
	public static String SOManager_Exception_Sender_Not_Null;
	public static String SOManager_Exception_Shared_Object_Description_Not_Null;
	public static String SOManager_Not_Found;
	public static String SOManager_Object;
	public static String SOManager_Receiver_Object;
	public static String SOManager_Sender_Object;
	public static String TCPServerSOContainerGroup_Container_For_Target;
	public static String TCPServerSOContainerGroup_Exception_Connect_Request_Null;
	public static String TCPServerSOContainerGroup_Invalid_Connect_Request;
	public static String TCPServerSOContainerGroup_Not_Found;
	public static String TCPServerSOContainerGroup_Target_Null;
	public static String TCPServerSOContainerGroup_Target_Path_Null;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// 
	}
}
