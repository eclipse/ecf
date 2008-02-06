/*******************************************************************************
 * Copyright (c) 2007 Versant Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Kuppe (mkuppe <at> versant <dot> com) - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.provider.jslp.identity;

import ch.ethz.iks.slp.ServiceType;
import ch.ethz.iks.slp.ServiceURL;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.util.StringUtils;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.discovery.identity.ServiceTypeID;
import org.eclipse.ecf.internal.provider.jslp.Messages;
import org.eclipse.osgi.util.NLS;

public class JSLPServiceTypeID extends ServiceTypeID {

	private static final String JSLP_DELIM = ":"; //$NON-NLS-1$

	private static final long serialVersionUID = -4558132760112793805L;

	private ServiceType st;

	protected JSLPServiceTypeID(Namespace namespace, String type) throws IDCreateException {
		super(namespace);
		try {
			st = new ServiceType(type);
			// verify that the ServiceType is proper
			Assert.isNotNull(st.toString());
			Assert.isTrue(!st.toString().equals("")); //$NON-NLS-1$

			namingAuthority = st.getNamingAuthority();
			String str = st.toString();

			// remove the naming authority from the string
			int namingStart = str.indexOf("."); //$NON-NLS-1$
			if (namingStart != -1) {
				String head = str.substring(0, namingStart);
				// Concrete type too which would be after the NA?
				String tail = ""; //$NON-NLS-1$
				int namingEnd = type.indexOf(JSLP_DELIM, namingStart);
				if (namingEnd != -1) {
					tail = str.substring(namingEnd, str.length());
				}
				str = head + tail;
			}

			services = StringUtils.split(str, JSLP_DELIM);
			scopes = DEFAULT_SCOPE; //TODO-mkuppe set the scope somehow
			protocols = DEFAULT_PROTO; //TODO-mkuppe set the scope somehow
			createType();
		} catch (Exception e) {
			throw new IDCreateException(NLS.bind(Messages.JSLPServiceTypeID_4, type));
		}
	}

	JSLPServiceTypeID(Namespace namespace, ServiceURL anURL, String[] scopes) throws IDCreateException {
		this(namespace, anURL.getServiceType().toString());

		if (scopes != null && scopes.length > 0) {
			this.scopes = scopes;
		}

		// set the protocol if provided
		String protocol = anURL.getProtocol();
		if (protocol != null) {
			protocols = new String[] {protocol};
			createType();
		}
	}

	JSLPServiceTypeID(JSLPNamespace namespace, IServiceTypeID type) {
		super(namespace, type);

		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < services.length; i++) {
			buf.append(services[i]);
			if (i == 1) {
				buf.append("."); //$NON-NLS-1$
				buf.append(namingAuthority);
			}
			buf.append(":"); //$NON-NLS-1$
		}
		// remove dangling colon
		String string = buf.toString();
		st = new ServiceType(string.substring(0, string.length() - 1));
	}

	/**
	 * @return the jSLP ServiceType
	 */
	public ServiceType getServiceType() {
		return st;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.identity.ServiceTypeID#getInternal()
	 */
	public String getInternal() {
		// remove the dangling colon if present
		String str = st.toString();
		Assert.isNotNull(str);
		if (str.endsWith(":")) { //$NON-NLS-1$
			Assert.isTrue(str.length() > 1);
			return str.substring(0, str.length() - 1);
		}
		return str;
	}
}
