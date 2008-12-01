/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.provider.remoteservice.generic;

import java.util.Dictionary;
import org.eclipse.ecf.internal.provider.remoteservice.Activator;
import org.eclipse.ecf.internal.provider.remoteservice.Messages;
import org.eclipse.ecf.remoteservice.IRemoteFilter;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.osgi.framework.*;

/**
 *
 */
public class RemoteFilterImpl implements IRemoteFilter {

	Filter filter;

	/**
	 * @param createFilter
	 */
	public RemoteFilterImpl(String createFilter) throws InvalidSyntaxException {
		if (createFilter == null)
			throw new InvalidSyntaxException(Messages.RemoteFilter_EXCEPTION_FILTER_NOT_NULL, createFilter);
		this.filter = Activator.getDefault().createFilter(createFilter);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.remoteservice.IRemoteFilter#match(org.eclipse.ecf.remoteservice.IRemoteServiceReference)
	 */
	public boolean match(IRemoteServiceReference reference) {
		if (reference == null)
			return false;
		if (reference instanceof RemoteServiceReferenceImpl) {
			RemoteServiceReferenceImpl impl = (RemoteServiceReferenceImpl) reference;
			return match(impl.getRegistration().properties);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.remoteservice.IRemoteFilter#match(java.util.Dictionary)
	 */
	public boolean match(Dictionary dictionary) {
		return filter.match(dictionary);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.remoteservice.IRemoteFilter#matchCase(java.util.Dictionary)
	 */
	public boolean matchCase(Dictionary dictionary) {
		return filter.matchCase(dictionary);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof RemoteFilterImpl)) {
			return false;
		}

		return this.toString().equals(obj.toString());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return this.toString().hashCode();
	}

	public String toString() {
		return filter.toString();
	}

	public boolean match(ServiceReference reference) {
		return false;
	}
}
