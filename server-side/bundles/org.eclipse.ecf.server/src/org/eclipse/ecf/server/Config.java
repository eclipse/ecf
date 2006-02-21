package org.eclipse.ecf.server;

import org.eclipse.osgi.util.NLS;

public class Config extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ecf.server.config"; //$NON-NLS-1$
	private Config() {
	}
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Config.class);
	}
	public static String serverconfigfile;
}
