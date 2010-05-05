/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.discovery.local;

import java.io.IOException;
import java.io.InputStream;

public interface IServiceEndpointDescriptionPublisher {

	public void publishServiceDescription(InputStream serviceEndpointDescriptionFile)
			throws IOException;

	public void publishServiceDescription(
			ServiceEndpointDescriptionImpl serviceDescription);

	public void unpublishServiceDescription(InputStream serviceEndpointDescriptionFile)
			throws IOException;

	public void unpublishServiceDescription(
			ServiceEndpointDescriptionImpl serviceDescription);

}
