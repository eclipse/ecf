/*******************************************************************************
 * Copyright (c) 2015 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Scott Lewis - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.remoteserviceadmin.ui.endpoint.model;

import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin.ImportRegistration;

/**
 * @since 3.3
 */
public class EndpointNode extends AbstractEndpointNode {

	private EndpointDescription endpointDescription;
	private ImportRegistrationNode importRegistrationNode;

	public EndpointNode(EndpointDescription ed) {
		this.endpointDescription = ed;
	}

	public EndpointNode(EndpointDescription ed, ImportRegistrationNode ir) {
		this.endpointDescription = ed;
		this.importRegistrationNode = ir;
	}

	public boolean equals(Object other) {
		if (other instanceof EndpointNode) {
			EndpointNode o = (EndpointNode) other;
			return endpointDescription.getId().equals(
					o.endpointDescription.getId());
		}
		return false;
	}

	public int hashCode() {
		return endpointDescription.getId().hashCode();
	}

	public EndpointDescription getEndpointDescription() {
		return endpointDescription;
	}

	public ImportRegistrationNode getImportReg() {
		return importRegistrationNode;
	}

	public ImportRegistration getImportRegistration() {
		return (importRegistrationNode == null) ? null : importRegistrationNode
				.getImportRegistration();
	}

	public boolean isImported() {
		return getImportRegistration() != null;
	}

	public void setEndpointDescription(EndpointDescription ed) {
		this.endpointDescription = ed;
	}

	public void setImportReg(ImportRegistrationNode ir) {
		this.importRegistrationNode = ir;
	}

}
