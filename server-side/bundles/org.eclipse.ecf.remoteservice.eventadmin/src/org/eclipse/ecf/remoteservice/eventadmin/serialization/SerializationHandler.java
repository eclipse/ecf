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
package org.eclipse.ecf.remoteservice.eventadmin.serialization;

import java.io.NotSerializableException;

/**
 * @since 1.2
 */
public abstract class SerializationHandler {
	public Object serialize(Object val) throws NotSerializableException {
		return val;
	}

	public Object deserialize(Object val) {
		return val;
	}


	public abstract Object getTopic();
}
