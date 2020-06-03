/****************************************************************************
 * Copyright (c) 2008 Versant Corp. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Versant Corp. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.discovery.ui.model.resource;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ecf.discovery.ui.model.ModelPlugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;


public class ServiceResourceFactory extends ResourceFactoryImpl {
	
	/**
	 * 
	 */
	private static final String EMF_FILE_NAME = "known.service";

	/* (non-Javadoc)
	 * @see org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl#createResource(org.eclipse.emf.common.util.URI)
	 */
	public Resource createResource(URI protocol) {
		IPath path = ModelPlugin.getDefault().getStateLocation();
		URI uri = URI.createFileURI(path.append(EMF_FILE_NAME).toOSString());
		return new ServiceResource(uri);
	}
}
