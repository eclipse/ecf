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

import org.eclipse.ecf.core.identity.*;
import org.eclipse.osgi.util.NLS;

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

	private String getInitFromExternalForm(Object[] args) {
		if (args == null || args.length < 1 || args[0] == null)
			return null;
		if (args[0] instanceof String) {
			String arg = (String) args[0];
			if (arg.startsWith(getScheme() + Namespace.SCHEME_SEPARATOR)) {
				int index = arg.indexOf(Namespace.SCHEME_SEPARATOR);
				if (index >= arg.length())
					return null;
				return arg.substring(index + 1);
			}
		}
		return null;
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
		try {
			String init = getInitFromExternalForm(parameters);
			if (init != null)
				return new R_OSGiID(init);
			return new R_OSGiID((String) parameters[0]);
		} catch (Exception e) {
			throw new IDCreateException(NLS.bind("{0} createInstance()", getName()), e); //$NON-NLS-1$
		}
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