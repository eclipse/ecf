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
package org.eclipse.ecf.examples.internal.eventadmin.app;

import java.io.NotSerializableException;

import org.eclipse.ecf.remoteservice.eventadmin.serialization.SerializationHandler;

public class ExampleSerializationHandler extends SerializationHandler {

	private static final String PREFIX = "ECF_ESH:";

	private final String topic;

	public ExampleSerializationHandler(final String topic) {
		this.topic = topic;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.remoteservice.eventadmin.serialization.SerializationHandler#serialize(java.lang.Object)
	 */
	public Object serialize(Object val) throws NotSerializableException {
		if (val instanceof NonSerializable) {
			final NonSerializable ns = (NonSerializable) val;
			return PREFIX + ns.getPayload();
		}
		return super.serialize(val);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.remoteservice.eventadmin.serialization.SerializationHandler#deserialize(java.lang.Object)
	 */
	public Object deserialize(Object val) {
		if (val instanceof String) {
			final String str = (String) val;
			// poor mans serialization
			if(str.startsWith(PREFIX)) {
				return new NonSerializable(str.substring(PREFIX.length()));
			}
		}
		return super.deserialize(val);
	}

	public Object getTopic() {
		return topic;
	}
}
