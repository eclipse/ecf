/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.core.sharedobject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.ecf.core.identity.ID;

/**
 * Description of a local ISharedObject instance.
 * 
 */
public class SharedObjectDescription implements Serializable {
	private static final long serialVersionUID = -999672007680512082L;

	protected SharedObjectTypeDescription typeDescription;

	protected ID id;

	protected Map<String, ?> properties = null;

	/**
	 * @since 2.6
	 */
	public SharedObjectDescription() {

	}

	/**
	 * @since 2.3
	 */
	public SharedObjectDescription(SharedObjectTypeDescription typeDescription, ID id, Map<String, ?> properties) {
		this.typeDescription = typeDescription;
		this.id = id;
		this.properties = (properties == null) ? new HashMap<String, Object>() : properties;
	}

	/**
	 * @since 2.3
	 */
	public SharedObjectDescription(SharedObjectTypeDescription typeDescription, ID id) {
		this(typeDescription, id, null);
	}

	/**
	 * @since 2.3
	 */
	public SharedObjectDescription(String typeName, ID id) {
		this(typeName, id, null);
	}

	public SharedObjectDescription(String typeName, ID id, Map<String, ?> properties) {
		this.typeDescription = new SharedObjectTypeDescription(typeName, null, null, null);
		this.id = id;
		this.properties = (properties == null) ? new HashMap<String, Object>() : properties;
	}

	public SharedObjectDescription(Class clazz, ID id, Map<String, ?> properties) {
		this.typeDescription = new SharedObjectTypeDescription(clazz.getName(), null);
		this.id = id;
		this.properties = (properties == null) ? new HashMap<String, Object>() : properties;
	}

	public SharedObjectTypeDescription getTypeDescription() {
		return typeDescription;
	}

	public ID getID() {
		return id;
	}

	public Map<String, ?> getProperties() {
		return properties;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("SharedObjectDescription["); //$NON-NLS-1$
		sb.append("typeDescription=").append(typeDescription); //$NON-NLS-1$ 
		sb.append(";id=").append(id); //$NON-NLS-1$ 
		sb.append(";props=").append(properties).append("]"); //$NON-NLS-1$ //$NON-NLS-2$ 
		return sb.toString();
	}
}