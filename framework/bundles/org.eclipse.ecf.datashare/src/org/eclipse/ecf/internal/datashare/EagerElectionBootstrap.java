/*******************************************************************************
 * Copyright (c) 2005 Peter Nehrer and Composent, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Peter Nehrer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.internal.datashare;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.ecf.core.ISharedObjectConfig;
import org.eclipse.ecf.core.SharedObjectDescription;
import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.events.ISharedObjectActivatedEvent;
import org.eclipse.ecf.core.events.ISharedObjectContainerDepartedEvent;
import org.eclipse.ecf.core.events.ISharedObjectContainerJoinedEvent;
import org.eclipse.ecf.core.events.ISharedObjectMessageEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.Event;

/**
 * @author pnehrer
 */
public class EagerElectionBootstrap implements IBootstrap {

	private final Random random = new Random();

	private Agent agent;

	private ISharedObjectConfig config;

	private ID coordinatorID;

	private Election election;

	private final Timer timer = new Timer();

	private TimerTask task;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.internal.datashare.IBootstrap#setAgent(org.eclipse.ecf.internal.datashare.Agent)
	 */
	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.internal.datashare.IBootstrap#init(org.eclipse.ecf.core.ISharedObjectConfig)
	 */
	public void init(ISharedObjectConfig config)
			throws SharedObjectInitException {
		this.config = config;
		Map params = config.getProperties();
		if (params != null) {
			Object param = params.get("coordinatorID");
			if (param != null)
				coordinatorID = (ID) param;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.internal.datashare.IBootstrap#handleEvent(org.eclipse.ecf.core.util.Event)
	 */
	public void handleEvent(Event event) {
		if (event instanceof ISharedObjectActivatedEvent) {
			ISharedObjectActivatedEvent e = (ISharedObjectActivatedEvent) event;
			if (e.getActivatedID().equals(config.getSharedObjectID()))
				handleActivated();
		} else if (event instanceof ISharedObjectContainerJoinedEvent) {
			ISharedObjectContainerJoinedEvent e = (ISharedObjectContainerJoinedEvent) event;
			if (!e.getJoinedContainerID().equals(e.getLocalContainerID()))
				handleJoined(e.getJoinedContainerID());
		} else if (event instanceof ISharedObjectContainerDepartedEvent) {
			ISharedObjectContainerDepartedEvent e = (ISharedObjectContainerDepartedEvent) event;
			if (!e.getDepartedContainerID().equals(e.getLocalContainerID()))
				handleDeparted(e.getDepartedContainerID());
		} else if (event instanceof ISharedObjectMessageEvent) {
			ISharedObjectMessageEvent e = (ISharedObjectMessageEvent) event;
			if (e.getData() instanceof Vote)
				handleVote((Vote) e.getData(), e.getRemoteContainerID());
			else if (e.getData() instanceof Elected)
				handleElected(e.getRemoteContainerID());
			else if (e.getData() instanceof Ping)
				handlePing();
		}
	}

	private synchronized void handleActivated() {
		if (config.getHomeContainerID().equals(
				config.getContext().getLocalContainerID())) {
			coordinatorID = config.getContext().getLocalContainerID();
			timer.schedule(task = new Pinger(), 1000, 1000); // TODO make configurable
		} else {
			timer.schedule(task = new PingWatch(), 2000); // TODO make configurable
		}
	}

	private synchronized void handleJoined(ID containerID) {
		// TODO what if election is pending?
		if (config.getContext().getLocalContainerID().equals(coordinatorID))
			agent.doBootstrap(containerID);
	}

	private synchronized void handleVote(Vote msg, ID containerID) {
		if (election != null) {
			switch (election.processVote(msg.getTicket(), containerID)) {
			case Election.WON:
				processVictory();
			case Election.LOST:
				election = null;
			}
		}
	}

	private synchronized void handleElected(ID containerID) {
		election = null;
		coordinatorID = containerID;
		timer.schedule(task = new PingWatch(), 2000);
	}

	private synchronized void handleDeparted(ID containerID) {
		if (containerID.equals(coordinatorID))
			startElection();
	}

	private void handlePing() {
		if (task != null)
			task.cancel();

		timer.schedule(task = new PingWatch(), 2000);
	}

	private synchronized void startElection() {
		if (task != null)
			task.cancel();

		List members = Arrays.asList(config.getContext().getGroupMemberIDs());
		members.remove(config.getContext().getLocalContainerID());
		if (members.isEmpty())
			processVictory();
		else {
			long ticket = random.nextLong(); // TODO strategize this
			election = new Election(ticket, members);
			try {
				config.getContext().sendMessage(null, new Vote(ticket, null));
			} catch (IOException e) {
				handleError(e);
			}
		}
	}

	private void processVictory() {
		try {
			config.getContext().sendMessage(null, new Elected(null));
			coordinatorID = config.getContext().getLocalContainerID();
			timer.schedule(task = new Pinger(), 1000, 1000);
		} catch (IOException e) {
			handleError(e);
		}
	}

	private void handleError(Throwable t) {
		t.printStackTrace();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.internal.datashare.IBootstrap#dispose(org.eclipse.ecf.core.identity.ID)
	 */
	public void dispose(ID containerID) {
		timer.cancel();
		config = null;
	}

	public SharedObjectDescription createDescription() {
		HashMap params = new HashMap(1);
		params.put("coordinatorID", coordinatorID);
		return new SharedObjectDescription(config.getSharedObjectID(),
				getClass(), params);
	}

	private class Pinger extends TimerTask {

		public void run() {
			try {
				config.getContext().sendMessage(null, new Ping());
			} catch (IOException e) {
				handleError(e);
			}
		}
	}

	private class PingWatch extends TimerTask {

		public void run() {
			startElection();
		}
	}
}
