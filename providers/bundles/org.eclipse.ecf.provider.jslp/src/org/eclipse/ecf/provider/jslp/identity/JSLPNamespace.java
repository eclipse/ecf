/****************************************************************************
 * Copyright (c) 2007 Versant Corp.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Markus Kuppe
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.provider.jslp.identity;

import ch.ethz.iks.slp.ServiceType;
import ch.ethz.iks.slp.ServiceURL;
import java.net.URI;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.discovery.identity.*;

public class JSLPNamespace extends Namespace {
	private static final String JSLP_SCHEME = "jslp"; //$NON-NLS-1$

	private static final long serialVersionUID = -3041453162456476102L;

	public static final String NAME = "ecf.namespace.slp"; //$NON-NLS-1$

	public JSLPNamespace() {
		super(NAME, "JSLP Namespace"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.identity.Namespace#createInstance(java.lang.Object[])
	 */
	public ID createInstance(Object[] parameters) {
		// error case
		if (parameters == null || parameters.length < 1 || parameters.length > 2) {
			throw new IDCreateException("Parameters cannot be null and must be of length 1 or 2"); //$NON-NLS-1$

			// error case
		} else if (parameters[0] == null || parameters[0].equals("")) { //$NON-NLS-1$
			throw new IDCreateException("First parameter cannot be null or empty String"); //$NON-NLS-1$

			// create by jSLP ServiceURL
		} else if (parameters.length == 2 && parameters[0] instanceof ServiceURL) {
			final ServiceURL anURL = (ServiceURL) parameters[0];
			final String[] scopes = (String[]) parameters[1];
			return new JSLPServiceTypeID(this, anURL, scopes);
			//			final String serviceName = (String) (parameters[1] != null ? parameters[1] : anURL.getHost());
			//			return null /*new JSLPServiceID(this, stid, serviceName)*/;

			// conversion call where conversion isn't necessary
		} else if (parameters.length == 1 && parameters[0] instanceof JSLPServiceID) {
			return (ID) parameters[0];

			// convert from IServiceID to IServiceTypeID, String
		} else if (parameters.length == 1 && parameters[0] instanceof IServiceID) {
			final IServiceID anId = (IServiceID) parameters[0];
			final Object[] newParams = new Object[2];
			newParams[0] = anId.getServiceTypeID();
			newParams[1] = anId.getName();
			return createInstance(newParams);

			// create by ECF discovery generic IServiceTypeID (but not JSLPServiceID!!!)
		} else if (parameters[0] instanceof IServiceTypeID) {
			final IServiceTypeID stid = (IServiceTypeID) parameters[0];
			parameters[0] = stid.getName();
			return createInstance(parameters);

			// create by jSLP ServiceType
		} else if (parameters.length == 1 && parameters[0] instanceof ServiceType) {
			return new JSLPServiceTypeID(this, (ServiceType) parameters[0]);
			//return new JSLPServiceID(this, stid, (String) parameters[1]);

			// create by jSLP ServiceType String representation (from external)
		} else if (parameters[0] instanceof String && ((String) parameters[0]).startsWith("service:")) { //$NON-NLS-1$
			parameters[0] = new ServiceType((String) parameters[0]);
			return createInstance(parameters);

			// create IServiceID by ECF discovery generic String representation
		} else if (parameters.length == 2 && parameters[0] instanceof String && ((String) parameters[0]).startsWith("_") && parameters[1] instanceof URI) { //$NON-NLS-1$
			final String type = (String) parameters[0];
			final URI anURI = (URI) parameters[1];
			final JSLPServiceTypeID serviceType = new JSLPServiceTypeID(this, new ServiceTypeID(this, type));
			return new JSLPServiceID(this, serviceType, anURI);

			// create IServiceTypeID by ECF discovery generic ServiceType
		} else if (parameters.length == 1 && parameters[0] instanceof String && ((String) parameters[0]).startsWith("_")) { //$NON-NLS-1$
			final String type = (String) parameters[0];
			return new JSLPServiceTypeID(this, new ServiceTypeID(this, type));

			// error case second parameter not a String
		} else if (parameters.length == 2 && parameters[1] != null && !(parameters[1] instanceof String)) {
			throw new IDCreateException("Second parameter must be of type String"); //$NON-NLS-1$

			// error case
		} else {
			throw new IDCreateException("Wrong JSLPServiceID creation parameters"); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.identity.Namespace#getScheme()
	 */
	public String getScheme() {
		return JSLP_SCHEME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.Namespace#getSupportedParameterTypesForCreateInstance()
	 */
	public Class[][] getSupportedParameterTypes() {
		return new Class[][] { {String.class}, {String.class, String.class}, {ServiceURL.class, String[].class, String.class}, {IServiceTypeID.class}, {IServiceID.class}, {ServiceType.class, String.class}};
	}
}
