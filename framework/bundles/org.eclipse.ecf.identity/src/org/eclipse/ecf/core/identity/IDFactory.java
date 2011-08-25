/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.identity;

import java.security.AccessController;
import java.util.*;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.internal.core.identity.Activator;

/**
 * A factory class for creating ID instances. This is the factory for plugins to
 * manufacture ID instances.
 * 
 */
public class IDFactory implements IIDFactory {
	public static final String SECURITY_PROPERTY = IDFactory.class.getName()
			+ ".security"; //$NON-NLS-1$

	private static final int IDENTITY_CREATION_ERRORCODE = 2001;

	private static Hashtable namespaces = new Hashtable();

	private static boolean securityEnabled = false;

	protected static IIDFactory instance = null;

	static {
		instance = new IDFactory();
		addNamespace0(new StringID.StringIDNamespace());
		addNamespace0(new GUID.GUIDNamespace());
		addNamespace0(new LongID.LongNamespace());
		addNamespace0(new URIID.URIIDNamespace());
	}

	private synchronized static void initialize() {
		if (!initialized) {
			Activator a = Activator.getDefault();
			if (a != null)
				a.setupNamespaceExtensionPoint();
			initialized = true;
		}
	}

	private static boolean initialized = false;

	public synchronized static IIDFactory getDefault() {
		return instance;
	}

	private IDFactory() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ecf.core.identity.IIDFactory#addNamespace(org.eclipse.ecf
	 * .core.identity.Namespace)
	 */
	public Namespace addNamespace(Namespace namespace) throws SecurityException {
		if (namespace == null)
			return null;
		checkPermission(new NamespacePermission(namespace.toString(),
				NamespacePermission.ADD_NAMESPACE));
		initialize();
		return addNamespace0(namespace);
	}

	public final static Namespace addNamespace0(Namespace namespace) {
		if (namespace == null)
			return null;
		return (Namespace) namespaces.put(namespace.getName(), namespace);
	}

	protected final static void checkPermission(
			NamespacePermission namespacepermission) throws SecurityException {
		if (securityEnabled)
			AccessController.checkPermission(namespacepermission);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ecf.core.identity.IIDFactory#containsNamespace(org.eclipse
	 * .ecf.core.identity.Namespace)
	 */
	public boolean containsNamespace(Namespace namespace)
			throws SecurityException {
		if (namespace == null)
			return false;
		checkPermission(new NamespacePermission(namespace.toString(),
				NamespacePermission.CONTAINS_NAMESPACE));
		initialize();
		return containsNamespace0(namespace);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.IIDFactory#getNamespaces()
	 */
	public List getNamespaces() {
		initialize();
		return new ArrayList(namespaces.values());
	}

	public final static boolean containsNamespace0(Namespace n) {
		if (n == null)
			return false;
		return namespaces.containsKey(n.getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ecf.core.identity.IIDFactory#getNamespace(org.eclipse.ecf
	 * .core.identity.Namespace)
	 */
	public Namespace getNamespace(Namespace namespace) throws SecurityException {
		if (namespace == null)
			return null;
		checkPermission(new NamespacePermission(namespace.toString(),
				NamespacePermission.GET_NAMESPACE));
		initialize();
		return getNamespace0(namespace);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ecf.core.identity.IIDFactory#getNamespaceByName(java.lang
	 * .String)
	 */
	public Namespace getNamespaceByName(String name) throws SecurityException {
		initialize();
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
		return createID(new GUID.GUIDNamespace(), new Integer[] { new Integer(
				length) });
	}

	protected static void logAndThrow(String s, Throwable t)
			throws IDCreateException {
		IDCreateException e = null;
		if (t != null) {
			e = new IDCreateException(s + ": " + t.getClass().getName() + ": " //$NON-NLS-1$ //$NON-NLS-2$
					+ t.getMessage(), t);
		} else {
			e = new IDCreateException(s);
		}
		Activator.getDefault().log(
				new Status(IStatus.ERROR, Activator.PLUGIN_ID,
						IDENTITY_CREATION_ERRORCODE, s, e));
		throw e;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ecf.core.identity.IIDFactory#createID(org.eclipse.ecf.core
	 * .identity.Namespace, java.lang.Object[])
	 */
	public ID createID(Namespace n, Object[] args) throws IDCreateException {
		// Verify namespace is non-null
		if (n == null)
			logAndThrow("Namespace cannot be null", null); //$NON-NLS-1$
		initialize();
		// Make sure that namespace is in table of known namespace. If not,
		// throw...we don't create any instances that we don't know about!
		Namespace ns = getNamespace0(n);
		if (ns == null)
			logAndThrow("Namespace " + n.getName() + " not found", null); //$NON-NLS-1$
		return ns.createInstance(args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.IIDFactory#createID(java.lang.String,
	 * java.lang.Object[])
	 */
	public ID createID(String namespaceName, Object[] args)
			throws IDCreateException {
		Namespace n = getNamespaceByName(namespaceName);
		if (n == null)
			throw new IDCreateException(
					"Namespace " + namespaceName + " not found"); //$NON-NLS-1$
		return createID(n, args);
	}

	public ID createID(Namespace namespace, String uri)
			throws IDCreateException {
		return createID(namespace, new Object[] { uri });
	}

	public ID createID(String namespace, String uri) throws IDCreateException {
		return createID(namespace, new Object[] { uri });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ecf.core.identity.IIDFactory#createStringID(java.lang.String)
	 */
	public ID createStringID(String idstring) throws IDCreateException {
		if (idstring == null)
			throw new IDCreateException("StringID cannot be null"); //$NON-NLS-1$
		return createID(new StringID.StringIDNamespace(),
				new String[] { idstring });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.IIDFactory#createLongID(long)
	 */
	public ID createLongID(long l) throws IDCreateException {
		return createID(new LongID.LongNamespace(), new Long[] { new Long(l) });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ecf.core.identity.IIDFactory#removeNamespace(org.eclipse.
	 * ecf.core.identity.Namespace)
	 */
	public Namespace removeNamespace(Namespace n) throws SecurityException {
		if (n == null)
			return null;
		checkPermission(new NamespacePermission(n.toString(),
				NamespacePermission.REMOVE_NAMESPACE));
		initialize();
		return removeNamespace0(n);
	}

	protected final static Namespace removeNamespace0(Namespace n) {
		if (n == null)
			return null;
		return (Namespace) namespaces.remove(n.getName());
	}
}