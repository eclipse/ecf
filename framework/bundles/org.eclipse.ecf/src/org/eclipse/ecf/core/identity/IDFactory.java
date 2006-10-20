/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.identity;

import java.net.URI;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.eclipse.ecf.core.util.AbstractFactory;
import org.eclipse.ecf.internal.core.Trace;

/**
 * A factory class for creating ID instances. This is the factory for plugins to
 * manufacture ID instances.
 * 
 */
public class IDFactory implements IIDFactory {
	private static final Trace debug = Trace.create("idfactory");
	public static final String SECURITY_PROPERTY = IDFactory.class.getName()
			+ ".security";
	private static Hashtable namespaces = new Hashtable();
	private static boolean securityEnabled = false;
	protected static IIDFactory instance = null;
	static {
		instance = new IDFactory();
		addNamespace0(new StringID.StringIDNamespace());
		addNamespace0(new GUID.GUIDNamespace());
		addNamespace0(new LongID.LongNamespace());
		try {
			securityEnabled = (System.getProperty(SECURITY_PROPERTY) != null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected IDFactory() {
	}

	public static IIDFactory getDefault() {
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.IIDFactory#addNamespace(org.eclipse.ecf.core.identity.Namespace)
	 */
	public Namespace addNamespace(Namespace n) throws SecurityException {
		if (n == null)
			return null;
		checkPermission(new NamespacePermission(n.toString(),
				NamespacePermission.ADD_NAMESPACE));
		return addNamespace0(n);
	}

	protected final static Namespace addNamespace0(Namespace n) {
		if (n == null)
			return null;
		return (Namespace) namespaces.put(n.getName(), n);
	}

	protected final static void checkPermission(NamespacePermission p)
			throws SecurityException {
		if (securityEnabled)
			AccessController.checkPermission(p);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.IIDFactory#containsNamespace(org.eclipse.ecf.core.identity.Namespace)
	 */
	public boolean containsNamespace(Namespace n) throws SecurityException {
		debug("containsNamespace(" + n + ")");
		if (n == null)
			return false;
		checkPermission(new NamespacePermission(n.toString(),
				NamespacePermission.CONTAINS_NAMESPACE));
		return containsNamespace0(n);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.IIDFactory#getNamespaces()
	 */
	public List getNamespaces() {
		debug("getNamespaces()");
		return new ArrayList(namespaces.values());
	}

	private static void debug(String msg) {
		if (Trace.ON && debug != null) {
			debug.msg(msg);
		}
	}

	private static void dumpStack(String msg, Throwable e) {
		if (Trace.ON && debug != null) {
			debug.dumpStack(e, msg);
		}
	}

	protected final static boolean containsNamespace0(Namespace n) {
		if (n == null)
			return false;
		return namespaces.containsKey(n.getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.IIDFactory#getNamespace(org.eclipse.ecf.core.identity.Namespace)
	 */
	public Namespace getNamespace(Namespace n) throws SecurityException {
		debug("getNamespace(" + n + ")");
		if (n == null)
			return null;
		checkPermission(new NamespacePermission(n.toString(),
				NamespacePermission.GET_NAMESPACE));
		return getNamespace0(n);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.IIDFactory#getNamespaceByName(java.lang.String)
	 */
	public Namespace getNamespaceByName(String name) throws SecurityException {
		debug("getNamespaceByName(" + name + ")");
		return getNamespace0(name);
	}

	protected final static Namespace getNamespace0(Namespace n) {
		if (n == null)
			return null;
		return (Namespace) namespaces.get(n.getName());
	}

	protected final static Namespace getNamespace0(String name) {
		if (name == null)
			return null;
		else
			return (Namespace) namespaces.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.IIDFactory#createGUID()
	 */
	public ID createGUID() throws IDCreateException {
		return createGUID(GUID.DEFAULT_BYTE_LENGTH);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.IIDFactory#createGUID(int)
	 */
	public ID createGUID(int length) throws IDCreateException {
		debug("createGUID(" + length + ")");
		Namespace n = new GUID.GUIDNamespace();
		return createID(n, new String[] { Namespace.class.getName(),
				Integer.class.getName() }, new Object[] { n,
				new Integer(length) });
	}

	protected static void log(String s) {
		debug(s);
	}

	protected static void logException(String s, Throwable t) {
		dumpStack(s, t);
	}

	protected static void logAndThrow(String s, Throwable t)
			throws IDCreateException {
		IDCreateException e = null;
		if (t != null) {
			e = new IDCreateException(s + ": " + t.getClass().getName()
					+ ": " + t.getMessage());
			e.setStackTrace(t.getStackTrace());
		} else {
			e = new IDCreateException(s);
		}
		logException(s, t);
		throw e;
	}

	protected static void logAndThrow(String s) throws IDCreateException {
		IDCreateException e = new IDCreateException(s);
		logException(s, null);
		throw e;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.IIDFactory#createID(org.eclipse.ecf.core.identity.Namespace,
	 *      java.lang.String[], java.lang.Object[])
	 */
	public ID createID(Namespace n, String[] argTypes, Object[] args)
			throws IDCreateException {
		debug("createID(" + n + "," + Trace.convertStringAToString(argTypes)
				+ "," + Trace.convertObjectAToString(args) + ")");
		// Verify namespace is non-null
		if (n == null)
			logAndThrow("Namespace cannot be null");
		// Make sure that namespace is in table of known namespace. If not,
		// throw...we don't create any instances that we don't know about!
		Namespace ns = getNamespace0(n);
		if (ns == null)
			logAndThrow("Namespace '" + n.getName() + "' not found");
		// We're OK, go ahead and setup array of classes for call to
		// instantiator
		Class clazzes[] = null;
		ClassLoader cl = ns.getClass().getClassLoader();
		try {
			clazzes = AbstractFactory.getClassesForTypes(argTypes, args, cl);
		} catch (ClassNotFoundException e) {
			logAndThrow("Exception in getClassesForTypes", e);
		}
		// Ask instantiator to actually create instance
		return ns.createInstance(clazzes, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.IIDFactory#createID(java.lang.String,
	 *      java.lang.String[], java.lang.Object[])
	 */
	public ID createID(String namespacename, String[] argTypes, Object[] args)
			throws IDCreateException {
		Namespace n = getNamespaceByName(namespacename);
		if (n == null)
			throw new IDCreateException("Namespace named "
					+ namespacename + " not found");
		return createID(n, argTypes, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.IIDFactory#createID(org.eclipse.ecf.core.identity.Namespace,
	 *      java.lang.Object[])
	 */
	public ID createID(Namespace n, Object[] args)
			throws IDCreateException {
		return createID(n, null, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.IIDFactory#createID(java.lang.String,
	 *      java.lang.Object[])
	 */
	public ID createID(String namespacename, Object[] args)
			throws IDCreateException {
		Namespace n = getNamespaceByName(namespacename);
		if (n == null)
			throw new IDCreateException("Namespace " + namespacename
					+ " not found");
		return createID(n, args);
	}

	public ID createID(Namespace namespace, URI uri) throws IDCreateException {
		return createID(namespace,new Object[] { uri });
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.IIDFactory#createID(java.lang.String,
	 *      java.net.URI)
	 */
	public ID createID(String namespacename, URI uri)
			throws IDCreateException {
		if (uri == null)
			throw new IDCreateException("Null uri not allowed");
		Namespace n = getNamespaceByName(namespacename);
		if (n == null)
			throw new IDCreateException("Namespace " + n + " not found");
		return createID(n, new Object[] { uri });
	}

	public ID createID(Namespace namespace, String uri) throws IDCreateException {
		return createID(namespace,new Object[] { uri });
	}
	public ID createID(String namespace, String uri) throws IDCreateException {
		return createID(namespace,new Object[] { uri });		
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.IIDFactory#createStringID(java.lang.String)
	 */
	public ID createStringID(String idstring) throws IDCreateException {
		if (idstring == null)
			throw new IDCreateException("String cannot be null");
		Namespace n = new StringID.StringIDNamespace();
		return createID(n, new String[] { String.class.getName() },
				new Object[] { idstring });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.IIDFactory#createLongID(java.lang.Long)
	 */
	public ID createLongID(Long l) throws IDCreateException {
		if (l == null)
			throw new IDCreateException("Long cannot be null");
		Namespace n = new LongID.LongNamespace();
		return createID(n, new String[] { String.class.getName() },
				new Object[] { l });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.IIDFactory#createLongID(long)
	 */
	public ID createLongID(long l) throws IDCreateException {
		Namespace n = new LongID.LongNamespace();
		return createID(n, new String[] { String.class.getName() },
				new Object[] { new Long(l) });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.IIDFactory#removeNamespace(org.eclipse.ecf.core.identity.Namespace)
	 */
	public Namespace removeNamespace(Namespace n) throws SecurityException {
		debug("removeNamespace(" + n + ")");
		if (n == null)
			return null;
		checkPermission(new NamespacePermission(n.toString(),
				NamespacePermission.REMOVE_NAMESPACE));
		return removeNamespace0(n);
	}

	protected final static Namespace removeNamespace0(Namespace n) {
		if (n == null)
			return null;
		return (Namespace) namespaces.remove(n);
	}
}