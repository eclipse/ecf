/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.internal.provider.r_osgi;

import org.eclipse.ecf.remoteservice.IRemoteFilter;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.osgi.framework.*;

/**
 *
 */
public class RemoteFilterImpl extends org.eclipse.ecf.remoteservice.util.RemoteFilterImpl implements IRemoteFilter {

	/**
	 * @param createFilter
	 */
	public RemoteFilterImpl(BundleContext context, String createFilter) throws InvalidSyntaxException {
		super(context, createFilter);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.remoteservice.IRemoteFilter#match(org.eclipse.ecf.remoteservice.IRemoteServiceReference)
	 */
	public boolean match(IRemoteServiceReference reference) {
		if (reference == null)
			return false;
		if (reference instanceof RemoteServiceReferenceImpl) {
			RemoteServiceReferenceImpl impl = (RemoteServiceReferenceImpl) reference;
			return match(impl.getProperties());
		}
		return false;
	}

	public boolean match(ServiceReference reference) {
		return false;
	}
}
