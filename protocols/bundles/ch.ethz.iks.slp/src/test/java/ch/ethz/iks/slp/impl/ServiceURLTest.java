/* Copyright (c) 2005-2008 Jan S. Rellermeyer
 * Systems Group,
 * Department of Computer Science, ETH Zurich.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    - Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    - Neither the name of ETH Zurich nor the names of its contributors may be
 *      used to endorse or promote products derived from this software without
 *      specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package ch.ethz.iks.slp.impl;

import ch.ethz.iks.slp.ServiceURL;
import junit.framework.TestCase;

public class ServiceURLTest extends TestCase {

	public ServiceURLTest() {
		super("ServiceURLTest");
		System.setProperty("net.slp.port", "10427");
	}
	
	public void testServiceURL1() throws Exception {
		String urlString = "service:test:myservice://localhost";
		ServiceURL url = new ServiceURL(urlString, 0);
		assertEquals(url.getServiceType().toString(), "service:test:myservice");
		assertEquals(url.getHost(), "localhost");
		assertEquals(url.getPort(), 0);
		assertEquals(url.getURLPath(), "");
		assertEquals(url.getUserInfo(), "");
		assertEquals(url.getProtocol(), null);
		assertEquals(url.toString(), urlString);
	}

	public void testServiceURL2() throws Exception {
		String urlString = "service:test:myservice://localhost:80";
		ServiceURL url = new ServiceURL(urlString, 0);
		assertEquals("service:test:myservice", url.getServiceType().toString());
		assertEquals("localhost", url.getHost());
		assertEquals(80, url.getPort());
		assertEquals("", url.getURLPath());
		assertEquals("", url.getUserInfo());
		assertEquals(null, url.getProtocol());
		assertEquals(urlString, url.toString());
	}

	public void testServiceURL3() throws Exception {
		String urlString = "service:test:myservice://localhost:80/path";
		ServiceURL url = new ServiceURL(urlString, 0);
		assertEquals(url.getServiceType().toString(), "service:test:myservice");
		assertEquals(url.getHost(), "localhost");
		assertEquals(url.getPort(), 80);
		assertEquals(url.getURLPath(), "/path");
		assertEquals(url.getUserInfo(), "");
		assertEquals(url.getProtocol(), null);
		assertEquals(url.toString(), urlString);
	}

	public void testServiceURL4() throws Exception {
		String urlString = "service:test:myservice://localhost/my/path";
		ServiceURL url = new ServiceURL(urlString, 0);
		assertEquals(url.getServiceType().toString(), "service:test:myservice");
		assertEquals(url.getHost(), "localhost");
		assertEquals(url.getPort(), 0);
		assertEquals(url.getURLPath(), "/my/path");
		assertEquals(url.getUserInfo(), "");
		assertEquals(url.getProtocol(), null);
		assertEquals(url.toString(), urlString);
	}

	public void testServiceURL5() throws Exception {
		String urlString = "service:test:myservice://http://localhost:8080/my/path";
		ServiceURL url = new ServiceURL(urlString, 0);
		assertEquals(url.getServiceType().toString(), "service:test:myservice");
		assertEquals(url.getHost(), "localhost");
		assertEquals(url.getPort(), 8080);
		assertEquals(url.getURLPath(), "/my/path");
		assertEquals(url.getUserInfo(), "");
		assertEquals(url.getProtocol(), "http");
		assertEquals(url.toString(), urlString);
	}

	public void testServiceURL6() throws Exception {
		String urlString = "service:test://http://localhost";
		ServiceURL url = new ServiceURL(urlString, 0);
		assertEquals(url.getServiceType().toString(), "service:test");
		assertEquals(url.getHost(), "localhost");
		assertEquals(url.getPort(), 0);
		assertEquals(url.getURLPath(), "");
		assertEquals(url.getUserInfo(), "");
		assertEquals(url.getProtocol(), "http");
		assertEquals(url.toString(), urlString);
	}

	public void testServiceURLNamingAuthorityCustom() throws Exception {
		String urlString = "service:test.foo://http://localhost";
		ServiceURL url = new ServiceURL(urlString, 0);
		assertEquals(url.getServiceType().toString(), "service:test.foo");
		assertEquals(url.getHost(), "localhost");
		assertEquals(url.getPort(), 0);
		assertEquals(url.getURLPath(), "");
		assertEquals(url.getUserInfo(), "");
		assertEquals(url.getProtocol(), "http");
		assertEquals(url.toString(), urlString);
		assertTrue("foo".equals(url.getServiceType().getNamingAuthority()));
	}
	
	public void testServiceURLNamingAuthorityDefault() throws Exception {
		String urlString = "service:test://http://localhost";
		ServiceURL url = new ServiceURL(urlString, 0);
		assertEquals(url.getServiceType().toString(), "service:test");
		assertEquals(url.getHost(), "localhost");
		assertEquals(url.getPort(), 0);
		assertEquals(url.getURLPath(), "");
		assertEquals(url.getUserInfo(), "");
		assertEquals(url.getProtocol(), "http");
		assertEquals(url.toString(), urlString);
		assertTrue("".equals(url.getServiceType().getNamingAuthority()));
	}
	
	public void testServiceURLNamingAuthorityIana() throws Exception {
		String urlString = "service:test.iana://http://localhost";
		ServiceURL url = new ServiceURL(urlString, 0);
		assertEquals(url.getServiceType().toString(), "service:test");
		assertEquals(url.getHost(), "localhost");
		assertEquals(url.getPort(), 0);
		assertEquals(url.getURLPath(), "");
		assertEquals(url.getUserInfo(), "");
		assertEquals(url.getProtocol(), "http");
		assertEquals(url.toString(), "service:test://http://localhost");
		assertTrue("".equals(url.getServiceType().getNamingAuthority()));
	}
	
	public void testServiceURLUserInfo() throws Exception {
		String urlString = "service:test.iana://http://foobar@localhost";
		ServiceURL url = new ServiceURL(urlString, 0);
		assertEquals(url.getServiceType().toString(), "service:test");
		assertEquals(url.getHost(), "localhost");
		assertEquals(url.getPort(), 0);
		assertEquals(url.getURLPath(), "");
		assertEquals(url.getProtocol(), "http");
		assertEquals(url.toString(), "service:test://http://foobar@localhost");
		assertEquals(url.getUserInfo(), "foobar");
		assertTrue("".equals(url.getServiceType().getNamingAuthority()));
	}
	
	// https://bugs.eclipse.org/258252
	public void testServiceURL258252a() throws Exception {
		String urlString = "service:test:myservice://localhost:80/my:path";
		ServiceURL url = new ServiceURL(urlString, 0);
		assertEquals(url.getServiceType().toString(), "service:test:myservice");
		assertEquals(url.getHost(), "localhost");
		assertEquals(url.getPort(), 80);
		assertEquals(url.getURLPath(), "/my:path");
		assertEquals(url.getUserInfo(), "");
		assertEquals(url.getProtocol(), null);
		assertEquals(url.toString(), urlString);
	}
	public void testServiceURL258252b() throws Exception {
		String urlString = "service:test:myservice://localhost/my:path";
		ServiceURL url = new ServiceURL(urlString, 0);
		assertEquals(url.getServiceType().toString(), "service:test:myservice");
		assertEquals(url.getHost(), "localhost");
		assertEquals(url.getPort(), 0);
		assertEquals(url.getURLPath(), "/my:path");
		assertEquals(url.getUserInfo(), "");
		assertEquals(url.getProtocol(), null);
		assertEquals(url.toString(), urlString);
	}
	public void testServiceURL258252c() throws Exception {
		String urlString = "service:test:myservice://localhost/foo/bar#path";
		ServiceURL url = new ServiceURL(urlString, 0);
		assertEquals(url.getServiceType().toString(), "service:test:myservice");
		assertEquals(url.getHost(), "localhost");
		assertEquals(url.getPort(), 0);
		assertEquals(url.getURLPath(), "/foo/bar#path");
		assertEquals(url.getUserInfo(), "");
		assertEquals(url.getProtocol(), null);
		assertEquals(url.toString(), urlString);
	}
}
