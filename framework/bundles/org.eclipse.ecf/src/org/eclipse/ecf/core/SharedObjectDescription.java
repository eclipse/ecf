/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.provider.ISharedObjectInstantiator;

/**
 * Description of a local ISharedObject instance.
 * 
 */
public class SharedObjectDescription implements Serializable {

	private static final long serialVersionUID = -999672007680512082L;

	protected String name;
	protected transient ISharedObjectInstantiator instantiator;
	protected ID id;
	protected String description;
	protected Map properties;
	
	public SharedObjectDescription(String name, ISharedObjectInstantiator inst, ID id,
			String desc, Map props) {
		this.name = name;
		this.instantiator = inst;
		this.id = id;
		this.description = desc;
		if (props == null) {
			this.properties = new HashMap();
		} else {
			this.properties = props;
		}
	}
	public SharedObjectDescription(String name, ISharedObjectInstantiator inst, String desc, Map props) {
		this(name,inst,null,desc,props);
	}
	public String getDescription() {
		return description;
	}
	public ISharedObjectInstantiator getInstantiator() {
		return instantiator;
	}
	public String getName() {
		return name;
	}
	public Map getProperties() {
		return properties;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setProperties(Map props) {
		this.properties = props;
	}
	public String toString() {
		StringBuffer sb = new StringBuffer("SharedObjectDescription[");
		sb.append("name:").append(name).append(";");
		sb.append("instantiator:").append(";");
		sb.append("description:").append(";");
		sb.append("props:").append(properties).append(";");
		return sb.toString();
	}
	public ID getID() {
		return id;
	}
	public void setID(ID theID) {
		this.id = theID;
	}
}