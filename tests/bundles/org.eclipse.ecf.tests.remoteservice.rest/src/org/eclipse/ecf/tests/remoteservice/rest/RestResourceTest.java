/******************************************************************************* 
* Copyright (c) 2009 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
*******************************************************************************/ 
package org.eclipse.ecf.tests.remoteservice.rest;

import java.util.List;

import junit.framework.TestCase;

import org.eclipse.ecf.internal.remoteservice.rest.ResourceRepresentationFactory;

public class RestResourceTest extends TestCase {
	
	private ResourceRepresentationFactory resourceFactory;

	protected void setUp() throws Exception {
		resourceFactory = ResourceRepresentationFactory.getDefault();
	}
	
	public void testCreation() {
		assertNotNull(resourceFactory);
	}
	
	public void testResourceCreation() {
		Object adapter = resourceFactory.getAdapter(List.class);
		assertTrue(adapter instanceof List);
		List resources = (List) adapter;		
		assertTrue(resources.size()>=1);
	}

}
