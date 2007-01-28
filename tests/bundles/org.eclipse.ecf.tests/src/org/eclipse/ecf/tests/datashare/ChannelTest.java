/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.tests.datashare;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.datashare.IChannel;
import org.eclipse.ecf.datashare.IChannelContainerAdapter;
import org.eclipse.ecf.datashare.IChannelListener;
import org.eclipse.ecf.datashare.events.IChannelEvent;
import org.eclipse.ecf.tests.ContainerAbstractTestCase;

public class ChannelTest extends ContainerAbstractTestCase {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.tests.connect.ContainerConnectTestCase#createServerAndClients()
	 */
	protected void createServerAndClients() throws Exception {
		clientCount = 5;
		super.createServerAndClients();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		createServerAndClients();
	}

	protected void clearClientEvents() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		cleanUpServerAndClients();
		super.tearDown();
		clearClientEvents();
	}

	protected IChannelContainerAdapter getChannelContainer(int clientIndex) {
		return (IChannelContainerAdapter) getClients()[clientIndex].getAdapter(IChannelContainerAdapter.class);
	}
	
	public void testGetChannelContainerAdapter() throws Exception {
		IChannelContainerAdapter channelContainer = getChannelContainer(0);
		assertNotNull(channelContainer);
	}
	
	public void testGetChannel() throws Exception {
		IChannelContainerAdapter channelContainer = getChannelContainer(0);
		IChannel channel = channelContainer.createChannel(getNewID("0"),getIChannelListener(),null);
		assertNotNull(channel);
		assertNotNull(channel.getID());
		assertNotNull(channel.getListener());
	}

	/**
	 * @return
	 */
	private IChannelListener getIChannelListener() throws Exception {
		return new IChannelListener() {
			public void handleChannelEvent(IChannelEvent event) {
				//System.out.println("handleChannelEvent("+event+")");
			}};
	}

	/**
	 * @return
	 */
	private ID getNewID(String id) throws IDCreateException {
		return IDFactory.getDefault().createStringID(id);
	}

}
