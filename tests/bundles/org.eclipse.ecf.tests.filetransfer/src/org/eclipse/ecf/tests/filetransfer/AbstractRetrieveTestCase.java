/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.tests.filetransfer;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.filetransfer.IFileTransferListener;
import org.eclipse.ecf.filetransfer.IIncomingFileTransfer;
import org.eclipse.ecf.filetransfer.IRetrieveFileTransferContainerAdapter;
import org.eclipse.ecf.filetransfer.events.IFileTransferConnectStartEvent;
import org.eclipse.ecf.filetransfer.events.IFileTransferEvent;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveDataEvent;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveDoneEvent;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveStartEvent;
import org.eclipse.ecf.filetransfer.identity.FileIDFactory;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.equinox.concurrent.future.TimeoutException;
import org.eclipse.osgi.util.NLS;

/**
 *
 */
public abstract class AbstractRetrieveTestCase extends TestCase {

	IRetrieveFileTransferContainerAdapter retrieveAdapter = null;

	protected List startConnectEvents = null;
	protected List startEvents = null;
	protected List dataEvents = null;
	protected List doneEvents = null;

	protected IIncomingFileTransfer incomingFileTransfer;

	protected boolean done = false;

	Object lock = new Object();

	protected long startTime;

	protected void trace(String msg) {
		Trace.trace(System.currentTimeMillis() - startTime, msg);
	}

	protected IRetrieveFileTransferContainerAdapter getRetrieveAdapter() throws Exception {
		return (IRetrieveFileTransferContainerAdapter) ContainerFactory.getDefault().createContainer().getAdapter(IRetrieveFileTransferContainerAdapter.class);
	}

	protected IFileID createFileID(URL url) throws Exception {
		return FileIDFactory.getDefault().createFileID(retrieveAdapter.getRetrieveNamespace(), url);
	}

	protected void handleUnexpectedEvent(IFileTransferEvent event) {
		trace("handleUnexpectedEvent(" + event + ")");
	}
	
	protected void handleStartConnectEvent(IFileTransferConnectStartEvent event) {
		trace("handleStartConnectEvent(" + event + ")");
		startConnectEvents.add(event);
	}

	protected void handleStartEvent(IIncomingFileTransferReceiveStartEvent event) {
		trace("handleStartEvent(" + event + ")");
		startEvents.add(event);
	}

	protected void handleDataEvent(IIncomingFileTransferReceiveDataEvent event) {
		trace("handleDataEvent(" + event + ")");
		dataEvents.add(event);
	}

	protected void handleDoneEvent(IIncomingFileTransferReceiveDoneEvent event) {
		trace("handleDoneEvent(" + event + ")");
		doneEvents.add(event);
		synchronized (lock) {
			done = true;
		}
	}

	protected IFileTransferListener createFileTransferListener() {
		return new IFileTransferListener() {
			public void handleTransferEvent(IFileTransferEvent event) {
				if (event instanceof IFileTransferConnectStartEvent) {
					handleStartConnectEvent((IFileTransferConnectStartEvent) event);
				} else if (event instanceof IIncomingFileTransferReceiveStartEvent) {
					handleStartEvent((IIncomingFileTransferReceiveStartEvent) event);
				} else if (event instanceof IIncomingFileTransferReceiveDataEvent) {
					handleDataEvent((IIncomingFileTransferReceiveDataEvent) event);
				} else if (event instanceof IIncomingFileTransferReceiveDoneEvent) {
					handleDoneEvent((IIncomingFileTransferReceiveDoneEvent) event);
				} else {
					handleUnexpectedEvent(event);
				}
			}

		};
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		retrieveAdapter = getRetrieveAdapter();
		startConnectEvents = new ArrayList(); // no all provider emit this one.
		startEvents = new ArrayList();
		dataEvents = new ArrayList();
		doneEvents = new ArrayList();
		synchronized (lock) {
			done = false;
		}
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		retrieveAdapter = null;
		startConnectEvents = null;
		startEvents = null;
		dataEvents = null;
		doneEvents = null;
		super.tearDown();
	}

    private static final String BANNER =
        "==================== {0} {1} ===================={2}";
	
    protected String getFullName() {
    	String name =getClass().getName();
        return name.substring(name.lastIndexOf('.') + 1) + '.' + getName();
    }
    
    public void runBare() throws Throwable {
        startTime = System.currentTimeMillis();
        trace(NLS.bind(BANNER, new Object[]{"RUNNING", getFullName(), ""}));
        Throwable throwable = null;
        try {
            super.runBare();
        } catch (Throwable t) {
        	throwable = t;
        	throw t;
        } finally {
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;
            trace(
                NLS.bind(BANNER, new Object[] {(throwable != null? "FAILED " : "PASSED "), getFullName(),
                " (elapsed time: " + elapsedTime + " ms)"}));
        }
    }

	protected void testRetrieve(URL fileToRetrieve) throws Exception {
		Assert.isNotNull(fileToRetrieve);
		retrieveAdapter.sendRetrieveRequest(createFileID(fileToRetrieve), createFileTransferListener(), null);
	}

	protected void waitForDone(int timeout) throws Exception {
		final long start = System.currentTimeMillis();
		synchronized (lock) {
			while (!done && ((System.currentTimeMillis() - start) < timeout)) {
				lock.wait(timeout / 20);
			}
			if (!done)
				throw new TimeoutException(timeout);
		}
	}

	protected void assertHasEvent(Collection collection, Class eventType) {
		assertHasEventCount(collection, eventType, 1);
	}

	protected void assertHasNoEvent(Collection collection, Class eventType) {
		assertHasEventCount(collection, eventType, 0);
	}

	protected void assertHasEventCount(Collection collection, Class eventType, int eventCount) {
		int count = 0;
		for (final Iterator i = collection.iterator(); i.hasNext();) {
			final Object o = i.next();
			if (eventType.isInstance(o))
				count++;
		}
		assertEquals(eventCount, count);
	}

	protected void assertHasMoreThanEventCount(Collection collection, Class eventType, int eventCount) {
		int count = 0;
		for (final Iterator i = collection.iterator(); i.hasNext();) {
			final Object o = i.next();
			if (eventType.isInstance(o))
				count++;
		}
		assertTrue(count > eventCount);
	}

	protected void addProxy(final String proxyHost, final int port, final String username, final String password) throws Exception {
		IProxyService proxyService = Activator.getDefault().getProxyService();
		proxyService.setProxiesEnabled(true);
		proxyService.setSystemProxiesEnabled(false);
		IProxyData proxyData = new IProxyData() {
	
			public void disable() {
			}
	
			public String getHost() {
				return proxyHost;
			}
	
			public String getPassword() {
				return password;
			}
	
			public int getPort() {
				return port;
			}
	
			public String getType() {
				return "HTTP";
			}
	
			public String getUserId() {
				return username;
			}
	
			public boolean isRequiresAuthentication() {
				return (username != null);
			}
	
			public void setHost(String host) {
			}
	
			public void setPassword(String password) {
			}
	
			public void setPort(int port) {
			}
	
			public void setUserid(String userid) {
			}

			//TODO: What is the current expected target
			public String getSource() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setSource(String source) {
				// TODO Auto-generated method stub
				
			}
			
		};
		proxyService.setProxyData(new IProxyData[] { proxyData });
	}

}
