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
import java.util.Hashtable;
import java.util.Map;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.provider.ISharedObjectInstantiator;

/**
 * Description of an ISharedObject instance.
 * 
 */
public class SharedObjectDescription implements Serializable {
	private static final long serialVersionUID = 3257848783613146929L;
	protected static long staticID = 0;
	protected ID id;
	protected ID homeID;
	protected String className;
	protected Map properties;
	protected long identifier;
	protected String name;
	protected transient ISharedObjectInstantiator instantiator;
	
	public static long getNextUniqueIdentifier() {
		return staticID++;
	}

	public ISharedObjectInstantiator getInstantiator() {
		try {
			instantiator = (ISharedObjectInstantiator) Class.forName(getClassname()).newInstance();
		} catch (Exception e) {
			return null;
		}
		return instantiator;
	}
	
	public ClassLoader getClassLoader() {
		return getInstantiator().getClass().getClassLoader();
	}
	
	public SharedObjectDescription(String name, ID objectID, ID homeID, String className, Map dict, long ident) {
		this.name = name;
		this.id = objectID;
		this.homeID = homeID;
		this.className = className;
		this.properties = dict;
		this.identifier = ident;
	}
	public SharedObjectDescription(ID objectID, ID homeID,
			String className, Map dict, long ident) {
		this.id = objectID;
		this.homeID = homeID;
		this.className = className;
		this.name = this.className;
		if (dict != null)
			this.properties = dict;
		else
			this.properties = new Hashtable();
		this.identifier = ident;
	}

	public SharedObjectDescription(ID id, Class clazz, Map dict, long ident) {
		this(id, clazz.getName(), dict, ident);
	}

	public SharedObjectDescription(ID id, String className, Map dict, long ident) {
		this(id, null, className, dict, ident);
	}

	public SharedObjectDescription(ID id, String className, Map dict) {
		this(id, null, className, dict, getNextUniqueIdentifier());
	}

	public SharedObjectDescription(ID id, Class clazz, Map dict) {
		this(id, clazz, dict, getNextUniqueIdentifier());
	}

	public SharedObjectDescription(ID id, String className, long ident) {
		this(id, null, className, null, ident);
	}

	public SharedObjectDescription(ID id, Class clazz, long ident) {
		this(id, clazz, null, ident);
	}

	public SharedObjectDescription(ID id, String className) {
		this(id, className, getNextUniqueIdentifier());
	}

	public SharedObjectDescription(ID id, Class clazz) {
		this(id, clazz, null);
	}

	public ID getID() {
		return id;
	}

	public void setID(ID theID) {
		this.id = theID;
	}

	public ID getHomeID() {
		return homeID;
	}

	public void setHomeID(ID theID) {
		this.homeID = theID;
	}

	public String getClassname() {
		return className;
	}

	public void setClassname(String theName) {
		this.className = theName;
	}

	public Map getProperties() {
		return properties;
	}

	public void setProperties(Map props) {
		this.properties = props;
	}

	public long getIdentifier() {
		return identifier;
	}

	public void setIdentifier(long identifier) {
		this.identifier = identifier;
	}

	public String getName() {
		return name;
	}
	public String toString() {
		StringBuffer sb = new StringBuffer("SharedObjectDescription[");
		sb.append("name:").append(name).append(";");
		sb.append("id:").append(id).append(";");
		sb.append("homeID:").append(homeID).append(";");
		sb.append("class:").append(className).append(";");
		sb.append("props:").append(properties).append(";");
		sb.append("ident:").append(identifier).append("]");
		return sb.toString();
	}
}