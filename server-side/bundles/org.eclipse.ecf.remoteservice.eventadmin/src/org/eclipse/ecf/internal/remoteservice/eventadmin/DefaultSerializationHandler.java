/****************************************************************************
 * Copyright (c) 2013 Markus Alexander Kuppe and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Markus Alexander Kuppe - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.remoteservice.eventadmin;

import java.io.Externalizable;
import java.io.NotSerializableException;
import java.io.Serializable;

import org.eclipse.ecf.remoteservice.eventadmin.serialization.SerializationHandler;

/**
 * The default is to serialize what is already serialized or externalizable
 * and fail fast for the rest
 */
public class DefaultSerializationHandler extends SerializationHandler {

	public static final SerializationHandler INST = new DefaultSerializationHandler();

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.remoteservice.eventadmin.serialization.SmartSerializationHandler#serialize(java.lang.Object)
	 */
	public Object serialize(Object val) throws NotSerializableException {
		if (!(val instanceof Serializable || val instanceof Externalizable)) {
			throw new NotSerializableException("Cannot serialize property value="+val);
		}
		return val;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.remoteservice.eventadmin.serialization.SmartSerializationHandler#deserialize(java.lang.Object)
	 */
	public Object deserialize(Object val) {
		return super.deserialize(val);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.remoteservice.eventadmin.serialization.SerializationHandler#getTopic()
	 */
	public Object getTopic() {
		return "/ECF__NoSuchTopicNoSuchName__ECF/"; // invalid characters will make sure that topic is unique
	}
}
