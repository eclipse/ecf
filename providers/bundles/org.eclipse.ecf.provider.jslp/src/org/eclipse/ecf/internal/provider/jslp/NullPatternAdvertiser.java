/*******************************************************************************
 * Copyright (c) 2008 Versant Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Kuppe (mkuppe <at> versant <dot> com) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.provider.jslp;

import ch.ethz.iks.slp.Advertiser;
import ch.ethz.iks.slp.ServiceURL;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import org.eclipse.ecf.core.util.Trace;

public class NullPatternAdvertiser implements Advertiser {

	/* (non-Javadoc)
	 * @see ch.ethz.iks.slp.Advertiser#addAttributes(ch.ethz.iks.slp.ServiceURL, java.util.Dictionary)
	 */
	public void addAttributes(ServiceURL url, Dictionary attributes) {
		Trace.trace(Activator.PLUGIN_ID, JSLPDebugOptions.METHODS_TRACING, getClass(), "addAttributes(ServiceURL, Dictionary)", Advertiser.class + " not present"); //$NON-NLS-1$//$NON-NLS-2$
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.slp.Advertiser#deleteAttributes(ch.ethz.iks.slp.ServiceURL, java.util.Dictionary)
	 */
	public void deleteAttributes(ServiceURL url, Dictionary attributeIds) {
		Trace.trace(Activator.PLUGIN_ID, JSLPDebugOptions.METHODS_TRACING, getClass(), "deleteAttributes(ServiceURL, Dictionary)", Advertiser.class + " not present"); //$NON-NLS-1$//$NON-NLS-2$
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.slp.Advertiser#deregister(ch.ethz.iks.slp.ServiceURL)
	 */
	public void deregister(ServiceURL url) {
		Trace.trace(Activator.PLUGIN_ID, JSLPDebugOptions.METHODS_TRACING, getClass(), "deregister(ServiceURL)", Advertiser.class + " not present"); //$NON-NLS-1$//$NON-NLS-2$
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.slp.Advertiser#deregister(ch.ethz.iks.slp.ServiceURL, java.util.List)
	 */
	public void deregister(ServiceURL url, List scopes) {
		Trace.trace(Activator.PLUGIN_ID, JSLPDebugOptions.METHODS_TRACING, getClass(), "deregister(ServiceURL, List)", Advertiser.class + " not present"); //$NON-NLS-1$//$NON-NLS-2$
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.slp.Advertiser#getLocale()
	 */
	public Locale getLocale() {
		Trace.trace(Activator.PLUGIN_ID, JSLPDebugOptions.METHODS_TRACING, getClass(), "getLocale()", Advertiser.class + " not present"); //$NON-NLS-1$//$NON-NLS-2$
		return Locale.getDefault();
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.slp.Advertiser#getMyIP()
	 */
	public InetAddress getMyIP() {
		Trace.trace(Activator.PLUGIN_ID, JSLPDebugOptions.METHODS_TRACING, getClass(), "getMyIP()", Advertiser.class + " not present"); //$NON-NLS-1$//$NON-NLS-2$
		try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			Trace.catching(Activator.PLUGIN_ID, JSLPDebugOptions.METHODS_TRACING, getClass(), "getMyIP()", e); //$NON-NLS-1$
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.slp.Advertiser#register(ch.ethz.iks.slp.ServiceURL, java.util.Dictionary)
	 */
	public void register(ServiceURL url, Dictionary attributes) {
		Trace.trace(Activator.PLUGIN_ID, JSLPDebugOptions.METHODS_TRACING, getClass(), "register(ServiceURL, Dictionary)", Advertiser.class + " not present"); //$NON-NLS-1$//$NON-NLS-2$
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.slp.Advertiser#register(ch.ethz.iks.slp.ServiceURL, java.util.List, java.util.Dictionary)
	 */
	public void register(ServiceURL url, List scopes, Dictionary attributes) {
		Trace.trace(Activator.PLUGIN_ID, JSLPDebugOptions.METHODS_TRACING, getClass(), "register(ServiceURL, List, Dictionary)", Advertiser.class + " not present"); //$NON-NLS-1$//$NON-NLS-2$
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.slp.Advertiser#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale locale) {
		Trace.trace(Activator.PLUGIN_ID, JSLPDebugOptions.METHODS_TRACING, getClass(), "setLocale(Locale)", Advertiser.class + " not present"); //$NON-NLS-1$//$NON-NLS-2$
	}
}
