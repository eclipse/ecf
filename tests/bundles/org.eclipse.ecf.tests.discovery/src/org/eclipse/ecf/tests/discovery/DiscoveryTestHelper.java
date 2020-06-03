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

package org.eclipse.ecf.tests.discovery;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

public abstract class DiscoveryTestHelper {
	public final static int WEIGHT = 43;
	public final static int PRIORITY = 42;
	public final static String SERVICENAME = "aServiceNAME";
	public final static String NAMINGAUTHORITY = "someNamingAuthority";
	public final static String SCOPE = "someScope";
	public final static String PROTOCOL = "someProtocol";
	public final static int PORT = 3282;
	public final static String USERNAME = System.getProperty("user.name", "testuser");
	public final static String PASSWORD = "testpassword";
	public final static String PATH = "/a/Path/to/Something";
	public final static String QUERY = "someQuery";
	public final static String FRAGMENT = "aFragment";

	public final static String[] SERVICES = new String[] {"ecf", "junit", "tests"};
	public final static String[] PROTOCOLS = new String[] {PROTOCOL};
	public final static String SERVICE_TYPE = "_" + SERVICES[0] + "._" + SERVICES[1] + "._" + SERVICES[2] + "._" + PROTOCOL + "." + SCOPE + "._" + NAMINGAUTHORITY;
	public final static long TTL = 3600; //TODO change to something different than DEFAULT_TTL
	public static String HOSTNAME;
	
	public static URI createDefaultURI(String aHostname) {
//TODO-mkuppe https://bugs.eclipse.org/216944
//		return URI.create(PROTOCOL + "://" + USERNAME + ":" + PASSWORD + "@" + aHostname + ":" + PORT + "/" + PATH + "?" + QUERY + "#" + FRAGMENT);
		return URI.create(PROTOCOLS[0] + "://"/* + USERNAME + "@" */+ aHostname + ":" + PORT + PATH);
	}
	
	static {
		try {
			HOSTNAME = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			HOSTNAME = "127.0.0.1";
		}
	}
}