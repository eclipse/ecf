package org.eclipse.ecf.tests.discovery;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

public class DiscoveryTestHelper {
	public final static String PROTOCOL = "tcp";
	public final static String SCOPE = "local";
	public final static String NAMINGAUTHORITY = "IANA";

	public final static String[] SERVICES = new String[] {"service", "ecf", "tests"};
	public final static String SERVICE_TYPE = "_service._ecf._tests._" + PROTOCOL + "." + SCOPE + "._" + NAMINGAUTHORITY;
	public final static String SERVICE_TYPE2 = "_service._ecf._tests2._" + "fooProtocol" + "." + "fooScope" + "._" + "fooNA";
	public final static String SERVICE_TYPE3 = "_service._ecf._tests3._" + "barProtocol" + "." + "barScope" + "._" + "barNA";

	public final static int PORT = 3282;
	public final static int PORT2 = 3283;
	public final static int PORT3 = 3284;
	
	public static URI createDefaultURI() {
		return createURI("foo://" + getAuthority() + "/");
	}
	public static URI createURI(String uri) {
		return URI.create(uri);
	}
	public static String getAuthority() {
		return System.getProperty("user.name") + "@" + getHost() + ":" + PORT;
	}
	public static String getHost() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return "localhost";
		}
	}
}
