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

package org.eclipse.ecf.presence;

import org.eclipse.ecf.core.identity.ID;

/**
 * Listener for text IM messages. Implementers of this interface are registered
 * via {@link IPresenceContainerAdapter#addMessageListener(IMessageListener)}
 * 
 * @see IPresenceContainerAdapter
 */
public interface IMessageListener {

	/**
	 * Handle message from remote user. This method will be called by some
	 * thread when a message is received.
	 * 
	 * @param fromID
	 *            the ID of the user sending the message
	 * @param toID
	 *            the ID of the user to receive the message
	 * @param type
	 *            the Type of the message
	 * @param subject
	 *            the subject of the message
	 * @param messageBody
	 *            the message body
	 */
	public void handleMessage(ID fromID, ID toID, Type type, String subject,
			String messageBody);

	/**
	 * Inner class describing Type of message received
	 * 
	 */
	public static class Type {

		private static final String NORMAL_NAME = "normal";

		private static final String CHAT_NAME = "chat";

		private static final String GROUP_CHAT_NAME = "group_chat";

		private static final String SYSTEM_NAME = "system";

		private static final String ERROR_NAME = "error";

		private final transient String name;

		// Protected constructor so that only subclasses are allowed to create
		// instances
		protected Type(String name) {
			this.name = name;
		}

		public static Type fromString(String itemType) {
			if (itemType == null)
				return null;
			if (itemType.equals(NORMAL_NAME)) {
				return NORMAL;
			} else if (itemType.equals(CHAT_NAME)) {
				return CHAT;
			} else if (itemType.equals(GROUP_CHAT_NAME)) {
				return GROUP_CHAT;
			} else if (itemType.equals(SYSTEM_NAME)) {
				return SYSTEM;
			} else if (itemType.equals(ERROR_NAME)) {
				return ERROR;
			} else
				return null;
		}

		public static final Type NORMAL = new Type(NORMAL_NAME);

		public static final Type CHAT = new Type(CHAT_NAME);

		public static final Type GROUP_CHAT = new Type(GROUP_CHAT_NAME);

		public static final Type SYSTEM = new Type(SYSTEM_NAME);

		public static final Type ERROR = new Type(ERROR_NAME);

		public String toString() {
			return name;
		}

		// This is to make sure that subclasses don't screw up these methods
		public final boolean equals(Object that) {
			return super.equals(that);
		}

		public final int hashCode() {
			return super.hashCode();
		}
	}

}
