package org.eclipse.ecf.tests.discovery;

public interface ITestConstants {
	public final String PROTOCOL = "tcp";
	public final String SCOPE = "local";
	public final String NAMINGAUTHORITY = "IANA";

	public final String[] SERVICES = new String[] {"service", "ecf", "tests"};
	public final String SERVICE_TYPE = "_service._ecf._tests._" + PROTOCOL + "." + SCOPE + "._" + NAMINGAUTHORITY;
	public final String SERVICE_TYPE2 = "_service._ecf._tests2._" + "fooProtocol" + "." + "fooScope" + "._" + "fooNA";
	public final String SERVICE_TYPE3 = "_service._ecf._tests3._" + "barProtocol" + "." + "barScope" + "._" + "barNA";

	public final String HOST = "localhost";
	public final int PORT = 3282;
	public final int PORT2 = 3283;
	public final int PORT3 = 3284;

	public final String URI = "ecf://" + System.getProperty("user.name") + "@localhost:" + PORT + "/";
}
