/*******************************************************************************
 * Copyright (c) 2015 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Scott Lewis - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.r_osgi.identity;

import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.ecf.core.identity.*;

/**
 * @since 3.5
 */
public class R_OSGiWSNamespace extends R_OSGiNamespace {

	private static final long serialVersionUID = -3460085239213524498L;

	public static final String NAME_WS = "ecf.namespace.r_osgi.ws"; //$NON-NLS-1$

	/**
	 * the namespace scheme.
	 */
	private static final String NAMESPACE_SCHEME_WS = "r-osgi.ws"; //$NON-NLS-1$

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
			new R_OSGiWSNamespace();
		}
		return instance;
	}

	/**
	 * constructor.
	 */
	public R_OSGiWSNamespace() {
		super(NAME_WS, "R-OSGi Http Namespace"); //$NON-NLS-1$
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
		try {
			String uriString = (String) parameters[0];
			if (uriString == null)
				throw new NullPointerException("URI parameter is null"); //$NON-NLS-1$
			if (!uriString.startsWith(NAMESPACE_SCHEME_WS) && !uriString.startsWith("http")) //$NON-NLS-1$
				throw new URISyntaxException(uriString, "URI must have " + NAMESPACE_SCHEME_WS + " as protocol"); //$NON-NLS-1$ //$NON-NLS-2$
			URI uri = new URI(uriString);
			return new R_OSGiWSID(false, uri.getHost(), uri.getPort());
		} catch (Exception e) {
			throw new IDCreateException(getName() + " createInstance()", e); //$NON-NLS-1$
		}
	}

	/**
	 * get the scheme of this namespace.
	 * 
	 * @return the scheme.
	 * @see org.eclipse.ecf.core.identity.Namespace#getScheme()
	 */
	public String getScheme() {
		return NAMESPACE_SCHEME_WS;
	}

	/**
	 * get all supported schemes.
	 * 
	 * @return an array of supported schemes.
	 * @see org.eclipse.ecf.core.identity.Namespace#getSupportedSchemes()
	 */
	public String[] getSupportedSchemes() {
		return new String[] {NAMESPACE_SCHEME_WS};
	}

}
