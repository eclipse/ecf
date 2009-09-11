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

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.ecf.remoteservice.rest.identity.RestID;
import org.eclipse.ecf.remoteservice.rest.identity.RestNamespace;

public class RestIDTest extends TestCase {
	
	public void testCompareToNamespace() {
		RestNamespace namespace = new RestNamespace(RestNamespace.NAME, null);
		try {
			RestID id = new RestID(namespace, new URL("http://www.twitter.com"));
			RestID id2 = new RestID(namespace, new URL("http://www.eclipse.org"));
			int comparedId = id.namespaceCompareTo(id2);
			int comparedId2 = id2.namespaceCompareTo(id);
			assertEquals(comparedId, comparedId2);
		} catch (MalformedURLException e) {
			fail();
		}
	}
	
	public void testNamespaceEquals() {
		RestNamespace namespace = new RestNamespace(RestNamespace.NAME, null);
		try {
			RestID id = new RestID(namespace, new URL("http://www.twitter.com"));
			RestID id2 = new RestID(namespace, new URL("http://www.eclipse.org"));
			assertFalse(id.namespaceEquals(id2));
			assertFalse(id2.namespaceEquals(id));
			id2 = new RestID(namespace, new URL("http://www.twitter.com"));
			assertTrue(id.namespaceEquals(id2));
			assertTrue(id2.namespaceEquals(id));
		} catch (MalformedURLException e) {
			fail();
		}		
	}
	
	public void testNamespaceGetName() {
		RestNamespace namespace = new RestNamespace(RestNamespace.NAME, null);
		try {
			String url = "http://www.twitter.com";
			RestID id = new RestID(namespace, new URL(url));
			assertEquals(url, id.namespaceGetName());
		} catch (MalformedURLException e) {
			fail();
		}
	}
	
	public void testNamespaceHashCode() {
		RestNamespace namespace = new RestNamespace(RestNamespace.NAME, null);
		try {
			RestID id = new RestID(namespace, new URL("http://www.twitter.com"));
			RestID id2 = new RestID(namespace, new URL("http://www.eclipse.org"));
			int hash1 = id.namespaceHashCode();
			int hash2 = id2.namespaceHashCode();
			assertFalse(hash1 == hash2);
			id2 = new RestID(namespace, new URL("http://www.twitter.com"));
			hash2 = id2.namespaceHashCode();
			assertEquals(hash1, hash2);
		} catch (MalformedURLException e) {
			fail();
		}		
	}
	
	public void testGetBaseUrl() {
		RestNamespace namespace = new RestNamespace(RestNamespace.NAME, null);		
		try {
			String baseUrl = "http://www.twitter.com";
			RestID id = new RestID(namespace, new URL(baseUrl));
			URL url = id.getBaseURL();
			assertEquals(new URL(baseUrl), url);
		} catch (MalformedURLException e) {
			fail();
		}	
	}

}
