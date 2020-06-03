/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.sync;

import org.eclipse.core.runtime.IAdaptable;


/**
 * Change message.  Instances of this interface
 * may be serialized to a byte [] so that they can be
 * communicated to remote processes.
 * 
 * @since 2.1
 */
public interface IModelChangeMessage extends IAdaptable {

	/**
	 * Serialize this message to byte [].
	 * @return byte [] that is serialized representation of this model change message.
	 * @throws SerializationException if this model change message
	 * cannot be serialized.
	 */
	public byte[] serialize() throws SerializationException;

}
