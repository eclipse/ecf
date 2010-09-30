/*******************************************************************************
 * Copyright (c) 2009 Markus Alexander Kuppe.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Alexander Kuppe (ecf-dev_eclipse.org <at> lemmster <dot> de) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.localdiscovery;

import java.util.Map;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.osgi.services.discovery.RemoteServiceEndpointDescription;
import org.eclipse.ecf.osgi.services.discovery.RemoteServicePublication;
import org.eclipse.ecf.osgi.services.discovery.ServiceEndpointDescription;

public class ServiceEndpointDescriptionFactory implements IAdapterFactory {

	private static final String ECF_IDENTITY_STRING_ID = "org.eclipse.ecf.core.identity.StringID"; //$NON-NLS-1$
	private static final String ECF_SP_ECT = "ecf.sp.ect"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object,
	 * java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType.equals(RemoteServiceEndpointDescription.class)
				&& adaptableObject instanceof ServiceEndpointDescription) {
			final ServiceEndpointDescription sed = (ServiceEndpointDescription) adaptableObject;
			final Map properties = sed.getProperties();
			Object obj1 = properties
					.get(RemoteServicePublication.ENDPOINT_CONTAINERID);
			final Object obj2 = properties
					.get(RemoteServicePublication.ENDPOINT_CONTAINERID_NAMESPACE);
			if (obj1 instanceof byte[]) {
				obj1 = new String(((byte[]) obj1));
			}
			if (obj2 != null && obj1 instanceof String && obj2 instanceof String) {
				// create the endpoint id
				final String endpointStr = (String) obj1;
				final String namespaceStr = (String) obj2;
				return new RemoteServiceEndpointDescriptionImpl(sed, IDFactory
						.getDefault().createID(namespaceStr, endpointStr));
			} else if(obj2 == null && obj1 instanceof String) {
				// create the endpoint id via the endpoint str for known containers
				final String endpointStr = (String) obj1;
				if (endpointStr.startsWith("ecftcp://")) { //$NON-NLS-1$
					properties.put(ECF_SP_ECT, "ecf.generic.server"); //$NON-NLS-1$
				} else if(endpointStr.startsWith("r-osgi://")) { //$NON-NLS-1$
					properties.put(ECF_SP_ECT, "ecf.r_osgi.peer"); //$NON-NLS-1$
				} else {
					return null;
				}
				return new RemoteServiceEndpointDescriptionImpl(sed, IDFactory
						.getDefault().createID(ECF_IDENTITY_STRING_ID, endpointStr), properties); //$NON-NLS-1$
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[] { ServiceEndpointDescription.class };
	}
}
