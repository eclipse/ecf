/*******************************************************************************
 * Copyright (c) 2008 Jan S. Rellermeyer, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan S. Rellermeyer - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.provider.r_osgi.identity;

import java.util.Arrays;
import org.eclipse.ecf.core.identity.*;

/**
 * The R-OSGi default transport namespace (r-osgi://).
 * 
 * @author Jan S. Rellermeyer, ETH Zurich
 */
public class R_OSGiNamespace extends Namespace {

	/**
	 * the serial UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * the namespace scheme.
	 */
	public static final String NAMESPACE_SCHEME = "r-osgi"; //$NON-NLS-1$

	/**
	 * the singleton instance of this namespace.
	 */
	private static Namespace instance;

	/**
	 * get the singleton instance of this namespace.
	 * 
	 * @return the instance.
	 */
	public static Namespace getDefault() {
		if (instance == null) {
			new R_OSGiNamespace();
		}
		return instance;
	}

	/**
	 * constructor.
	 */
	public R_OSGiNamespace() {
		initialize("r-osgi", "R-OSGi Namespace"); //$NON-NLS-1$ //$NON-NLS-2$
		instance = this;
	}

	/**
	 * create a new ID within this namespace.
	 * 
	 * @param parameters
	 *            the parameter to pass to the ID.
	 * @return the new ID
	 * @throws IDCreateException
	 *             if the creation fails.
	 * @see org.eclipse.ecf.core.identity.Namespace#createInstance(java.lang.Object[])
	 */
	public ID createInstance(final Object[] parameters) throws IDCreateException {
		if (parameters == null || parameters.length != 1 || !(parameters[0] instanceof String)) {
			throw new IDCreateException("Cannot create ID from " + (parameters == null ? "null" : Arrays.asList(parameters).toString())); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return new R_OSGiID((String) parameters[0]);
	}

	/**
	 * get the scheme of this namespace.
	 * 
	 * @return the scheme.
	 * @see org.eclipse.ecf.core.identity.Namespace#getScheme()
	 */
	public String getScheme() {
		return NAMESPACE_SCHEME;
	}

	/**
	 * get all supported schemes.
	 * 
	 * @return an array of supported schemes.
	 * @see org.eclipse.ecf.core.identity.Namespace#getSupportedSchemes()
	 */
	public String[] getSupportedSchemes() {
		return new String[] {NAMESPACE_SCHEME};
	}

}