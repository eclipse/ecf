/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
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

	protected Map properties = new HashMap();

	protected SharedObjectDescription(
			SharedObjectTypeDescription typeDescription, ID id, Map properties) {
		this.typeDescription = typeDescription;
		this.id = id;
		if (properties != null) this.properties = properties;
	}

	protected SharedObjectDescription(
			SharedObjectTypeDescription typeDescription, ID id) {
		this(typeDescription, id, null);
	}

	public SharedObjectDescription(String typeName, ID id, Map properties) {
		this.typeDescription = new SharedObjectTypeDescription(typeName, null,
				null, null);
		this.id = id;
		if (properties != null) this.properties = properties;
	}

	public SharedObjectDescription(Class clazz, ID id, Map properties) {
		this.typeDescription = new SharedObjectTypeDescription(clazz.getName(),
				null);
		this.id = id;
		if (properties != null) this.properties = properties;
	}

	public SharedObjectTypeDescription getTypeDescription() {
		return typeDescription;
	}

	public ID getID() {
		return id;
	}

	public Map getProperties() {
		return properties;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("SharedObjectDescription[");
		sb.append("type=").append(typeDescription).append(";");
		sb.append("id=").append(id).append(";");
		sb.append("props=").append(properties).append(";");
		return sb.toString();
	}
}