/****************************************************************************
 * Copyright (c) 2009 EclipseSource and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.remoteservice.rest;

import java.net.URI;
import java.net.URL;

import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainerFactory;
import org.eclipse.ecf.tests.ECFAbstractTestCase;

public class RestContainerInstantiatorTest extends ECFAbstractTestCase {
	
	private IContainerFactory containerFactory;
	
	private ContainerTypeDescription description;

	protected void setUp() throws Exception {
		containerFactory = getContainerFactory();
		description = containerFactory.getDescriptionByName(RestConstants.REST_CONTAINER_TYPE);
	}
	
	public void testSupportedParameterTypes() {		
		Class[][] types = description.getSupportedParameterTypes();
		boolean foundString = false;
		boolean foundURL = false;
		boolean foundURI = false;
		for(int i=0; i < types.length; i++) {
			for(int j=0; j < types[i].length; j++) {
				if (types[i][j].equals(String.class)) foundString = true;
				if (types[i][j].equals(URL.class)) foundURL = true;
				if (types[i][j].equals(URI.class)) foundURI = true;
			}
		}
		assertTrue(foundString && foundURL && foundURI);
	}
	
}
