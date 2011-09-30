/*******************************************************************************
* Copyright (c) 2011 IBM, and others. 
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   IBM Corporation - initial API and implementation
******************************************************************************/

package org.eclipse.ecf.internal.provider.filetransfer.httpclient;

import java.util.Map;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.filetransfer.IRetrieveFileTransferOptions;
import org.eclipse.ecf.provider.filetransfer.httpclient.HttpClientOptions;

public class ConnectionManagerHelper {

	public static final int DEFAULT_CONNECTION_TIMEOUT = HttpClientOptions.RETRIEVE_DEFAULT_CONNECTION_TIMEOUT;
	public static final int DEFAULT_READ_TIMEOUT = HttpClientOptions.RETRIEVE_DEFAULT_READ_TIMEOUT;

	private MultiThreadedHttpConnectionManager connectionManager;

	public ConnectionManagerHelper() {
	}

	private static int getIntegerProperty(String prop, int intDefault) {
		int retVal = intDefault;
		String systemProp = System.getProperty(prop);
		if (systemProp != null) {
			try {
				retVal = Integer.parseInt(systemProp);
			} catch (NumberFormatException e) {
				Trace.trace(Activator.PLUGIN_ID, "Bad value for property '" + prop + "' : " + e + ". Using default value " + intDefault + "."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
		}
		return retVal;
	}

	private static long getLongProperty(String prop, long longDefault) {
		long retVal = longDefault;
		String systemProp = System.getProperty(prop);
		if (systemProp != null) {
			try {
				retVal = Long.parseLong(systemProp);
			} catch (NumberFormatException e) {
				Trace.trace(Activator.PLUGIN_ID, "Bad value for property '" + prop + "' : " + e + ". Using default value " + longDefault + "."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
		}
		return retVal;
	}

	private static boolean shouldReuseConnectionsOption() {
		boolean retVal = ConnectionOptions.REUSE_CONNECTIONS_DEFAULT;
		String systemProp = System.getProperty(ConnectionOptions.PROP_REUSE_CONNECTIONS);
		if (systemProp != null) {
			retVal = Boolean.valueOf(systemProp).booleanValue();
		}
		return retVal;
	}

	public static int getConnectTimeout(final Map options) {
		int result = DEFAULT_CONNECTION_TIMEOUT;
		Map localOptions = options;
		if (localOptions != null) {
			// See if the connect timeout option is present, if so set
			Object o = localOptions.get(IRetrieveFileTransferOptions.CONNECT_TIMEOUT);
			if (o != null) {
				if (o instanceof Integer) {
					result = ((Integer) o).intValue();
				} else if (o instanceof String) {
					result = new Integer(((String) o)).intValue();
				}
				return result;
			}
			o = localOptions.get("org.eclipse.ecf.provider.filetransfer.httpclient.retrieve.connectTimeout"); //$NON-NLS-1$
			if (o != null) {
				if (o instanceof Integer) {
					result = ((Integer) o).intValue();
				} else if (o instanceof String) {
					result = new Integer(((String) o)).intValue();
				}
			}
		}
		return result;
	}

	public static int getSocketReadTimeout(Map options) {
		int result = DEFAULT_READ_TIMEOUT;
		Map localOptions = options;
		if (localOptions != null) {
			// See if the connect timeout option is present, if so set
			Object o = localOptions.get(IRetrieveFileTransferOptions.READ_TIMEOUT);
			if (o != null) {
				if (o instanceof Integer) {
					result = ((Integer) o).intValue();
				} else if (o instanceof String) {
					result = new Integer(((String) o)).intValue();
				}
				return result;
			}
			o = localOptions.get("org.eclipse.ecf.provider.filetransfer.httpclient.retrieve.readTimeout"); //$NON-NLS-1$
			if (o != null) {
				if (o instanceof Integer) {
					result = ((Integer) o).intValue();
				} else if (o instanceof String) {
					result = new Integer(((String) o)).intValue();
				}
			}
		}
		return result;
	}

	public synchronized void initConnectionManager(HttpClient httpClient, Map options) {
		if (!shouldReuseConnectionsOption()) {
			Trace.trace(Activator.PLUGIN_ID, "Connections are not reused. To reuse connections set system property '" + ConnectionOptions.PROP_REUSE_CONNECTIONS + "' to true."); //$NON-NLS-1$ //$NON-NLS-2$
			initParameters(httpClient, new MultiThreadedHttpConnectionManager(), false, options);
			return;
		}
		if (connectionManager == null) {
			connectionManager = new MultiThreadedHttpConnectionManager();
			Trace.trace(Activator.PLUGIN_ID, "Created shared connection manager."); //$NON-NLS-1$ 
		} else {
			Trace.trace(Activator.PLUGIN_ID, "Reusing shared connection manager."); //$NON-NLS-1$ 
		}
		initParameters(httpClient, connectionManager, true, options);
	}

	private static void initParameters(HttpClient httpClient, HttpConnectionManager cm, boolean cmIsShared, Map options) {

		if (cmIsShared) {
			long closeIdlePeriod = getLongProperty(ConnectionOptions.PROP_POOL_CLOSE_IDLE_PERIOD, ConnectionOptions.POOL_CLOSE_IDLE_PERIOD_DEFAULT);
			if (closeIdlePeriod > 0) {
				Trace.trace(Activator.PLUGIN_ID, "Closing connections which were idle at least " + closeIdlePeriod + " milliseconds."); //$NON-NLS-1$ //$NON-NLS-2$
				cm.closeIdleConnections(closeIdlePeriod);
			}
		}

		// HttpClient parameters can be traced independently
		httpClient.setHttpConnectionManager(cm);
		int readTimeout = getSocketReadTimeout(options);
		cm.getParams().setSoTimeout(readTimeout);
		int connectTimeout = getConnectTimeout(options);
		cm.getParams().setConnectionTimeout(connectTimeout);

		if (cmIsShared) {
			HttpConnectionManagerParams cmParams = cm.getParams();
			int maxHostConnections = getIntegerProperty(ConnectionOptions.PROP_MAX_CONNECTIONS_PER_HOST, ConnectionOptions.MAX_CONNECTIONS_PER_HOST_DEFAULT);
			int maxTotalConnections = getIntegerProperty(ConnectionOptions.PROP_MAX_TOTAL_CONNECTIONS, ConnectionOptions.MAX_TOTAL_CONNECTIONS_DEFAULT);

			cmParams.setDefaultMaxConnectionsPerHost(maxHostConnections);
			cmParams.setMaxTotalConnections(maxTotalConnections);
			long connectionManagerTimeout = getLongProperty(ConnectionOptions.PROP_POOL_CONNECTION_TIMEOUT, ConnectionOptions.POOL_CONNECTION_TIMEOUT_DEFAULT);
			httpClient.getParams().setConnectionManagerTimeout(connectionManagerTimeout);
		}
	}

	public synchronized void shutdown() {
		if (connectionManager != null) {
			connectionManager.shutdown();
			connectionManager = null;
		}
	}

}
