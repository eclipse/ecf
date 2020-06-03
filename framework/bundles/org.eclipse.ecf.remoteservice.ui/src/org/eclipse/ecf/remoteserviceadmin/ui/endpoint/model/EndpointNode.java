/****************************************************************************
 * Copyright (c) 2015 Composent, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Scott Lewis - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.remoteserviceadmin.ui.endpoint.model;

import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin.ImportReference;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin.ImportRegistration;

/**
 * @since 3.3
 */
public class EndpointNode extends AbstractEndpointNode {

	private EndpointDescription endpointDescription;
	private ImportReference importReference;

	public EndpointNode(EndpointDescription ed) {
		this.endpointDescription = ed;
	}

	public EndpointNode(EndpointDescription ed, ImportRegistrationNode irn) {
		this.endpointDescription = ed;
		this.importReference = (ImportReference) irn.getImportRegistration().getImportReference();
	}

	/**
	 * @since 3.3
	 */
	public EndpointNode(EndpointDescription ed, ImportReference ir) {
		this.endpointDescription = ed;
		this.importReference = ir;
	}

	public boolean equals(Object other) {
		if (other instanceof EndpointNode) {
			EndpointNode o = (EndpointNode) other;
			return endpointDescription.getId().equals(o.endpointDescription.getId());
		}
		return false;
	}

	public int hashCode() {
		return endpointDescription.getId().hashCode();
	}

	public EndpointDescription getEndpointDescription() {
		return endpointDescription;
	}

	@Deprecated
	public ImportRegistrationNode getImportReg() {
		return null;
	}

	@Deprecated
	public ImportRegistration getImportRegistration() {
		return null;
	}

	/**
	 * @since 3.3
	 */
	public ImportReference getImportReference() {
		return importReference;
	}

	public boolean isImported() {
		return getImportReference() != null;
	}

	public void setEndpointDescription(EndpointDescription ed) {
		this.endpointDescription = ed;
	}

	/**
	 * @since 3.3
	 */
	public void setImportReference(ImportReference iref) {
		this.importReference = iref;
	}

	@Deprecated
	public void setImportReg(ImportRegistrationNode ir) {

	}

}
