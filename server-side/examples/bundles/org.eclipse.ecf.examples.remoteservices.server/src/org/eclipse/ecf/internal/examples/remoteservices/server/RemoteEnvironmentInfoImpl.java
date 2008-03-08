/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.internal.examples.remoteservices.server;

import org.eclipse.ecf.examples.remoteservices.common.IRemoteEnvironmentInfo;
import org.eclipse.osgi.service.environment.EnvironmentInfo;

/**
 *
 */
public class RemoteEnvironmentInfoImpl implements IRemoteEnvironmentInfo {

	private EnvironmentInfo getEnvironmentInfo() {
		return Activator.getDefault().getEnvironmentInfo();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.examples.remoteservices.common.IRemoteEnvironmentInfo#getCommandLineArgs()
	 */
	public String[] getCommandLineArgs() {
		final EnvironmentInfo ei = getEnvironmentInfo();
		System.out.println("getCommandLineArgs()");
		return (ei == null) ? null : ei.getCommandLineArgs();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.examples.remoteservices.common.IRemoteEnvironmentInfo#getFrameworkArgs()
	 */
	public String[] getFrameworkArgs() {
		final EnvironmentInfo ei = getEnvironmentInfo();
		System.out.println("getFrameworkArgs()");
		return (ei == null) ? null : ei.getFrameworkArgs();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.examples.remoteservices.common.IRemoteEnvironmentInfo#getNL()
	 */
	public String getNL() {
		final EnvironmentInfo ei = getEnvironmentInfo();
		System.out.println("getNL()");
		return (ei == null) ? null : ei.getNL();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.examples.remoteservices.common.IRemoteEnvironmentInfo#getNonFrameworkArgs()
	 */
	public String[] getNonFrameworkArgs() {
		final EnvironmentInfo ei = getEnvironmentInfo();
		System.out.println("getNonFrameworkArgs()");
		return (ei == null) ? null : ei.getNonFrameworkArgs();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.examples.remoteservices.common.IRemoteEnvironmentInfo#getOS()
	 */
	public String getOS() {
		final EnvironmentInfo ei = getEnvironmentInfo();
		System.out.println("getOS()");
		return (ei == null) ? null : ei.getOS();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.examples.remoteservices.common.IRemoteEnvironmentInfo#getOSArch()
	 */
	public String getOSArch() {
		final EnvironmentInfo ei = getEnvironmentInfo();
		System.out.println("getOSArch()");
		return (ei == null) ? null : ei.getOSArch();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.examples.remoteservices.common.IRemoteEnvironmentInfo#getProperty(java.lang.String)
	 */
	public String getProperty(String key) {
		final EnvironmentInfo ei = getEnvironmentInfo();
		System.out.println("getProperty(" + key + ")");
		return (ei == null) ? null : ei.getProperty(key);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.examples.remoteservices.common.IRemoteEnvironmentInfo#getWS()
	 */
	public String getWS() {
		final EnvironmentInfo ei = getEnvironmentInfo();
		System.out.println("getWS()");
		return (ei == null) ? null : ei.getWS();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.examples.remoteservices.common.IRemoteEnvironmentInfo#inDebugMode()
	 */
	public Boolean inDebugMode() {
		final EnvironmentInfo ei = getEnvironmentInfo();
		System.out.println("inDebugMode()");
		return (ei == null) ? null : new Boolean(ei.inDebugMode());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.examples.remoteservices.common.IRemoteEnvironmentInfo#inDevelopmentMode()
	 */
	public Boolean inDevelopmentMode() {
		final EnvironmentInfo ei = getEnvironmentInfo();
		System.out.println("inDevelopmentMode()");
		return (ei == null) ? null : new Boolean(ei.inDevelopmentMode());
	}

}
