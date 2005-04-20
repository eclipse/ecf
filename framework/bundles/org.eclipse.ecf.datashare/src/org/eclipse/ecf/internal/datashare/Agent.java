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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.eclipse.ecf.core.ISharedObject;
import org.eclipse.ecf.core.ISharedObjectConfig;
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
public class Agent implements ISharedObject {

	private final Random random = new Random();

	private Object sharedData;

	private ISharedObjectConfig config;

	private final HashMap elections = new HashMap();

	public Agent() {
	}

	public Agent(Object sharedData) {
		this.sharedData = sharedData;
	}

	public Object getSharedData() {
		return sharedData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObject#init(org.eclipse.ecf.core.ISharedObjectConfig)
	 */
	public synchronized void init(ISharedObjectConfig config)
			throws SharedObjectInitException {
		this.config = config;
		Map params = config.getProperties();
		if (params != null)
			sharedData = params.get("sharedData");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObject#handleEvent(org.eclipse.ecf.core.util.Event)
	 */
	public void handleEvent(Event event) {
		if (event instanceof ISharedObjectActivatedEvent) {
			ISharedObjectActivatedEvent e = (ISharedObjectActivatedEvent) event;
			if (e.getActivatedID().equals(config.getSharedObjectID()))
				handleActivated();
		} else if (event instanceof ISharedObjectContainerJoinedEvent) {
			ISharedObjectContainerJoinedEvent e = (ISharedObjectContainerJoinedEvent) event;
			handleJoined(e.getJoinedContainerID());
		} else if (event instanceof ISharedObjectContainerDepartedEvent) {
			ISharedObjectContainerDepartedEvent e = (ISharedObjectContainerDepartedEvent) event;
			handleDeparted(e.getDepartedContainerID());
		} else if (event instanceof ISharedObjectMessageEvent) {
			ISharedObjectMessageEvent e = (ISharedObjectMessageEvent) event;
			if (e.getData() instanceof Vote)
				handleVote((Vote) e.getData(), e.getRemoteContainerID());
			else if (e.getData() instanceof Elected)
				handleElected((Elected) e.getData());
		}
	}

	private void handleActivated() {
		// tell client we're ready
	}

	private void handleJoined(ID containerID) {
		long ticket = random.nextLong();
		Election election = new Election(ticket, config.getContext()
				.getGroupMemberIDs());
		synchronized (elections) {
			elections.put(containerID, election);
		}

		try {
			config.getContext()
					.sendMessage(null, new Vote(ticket, containerID));
		} catch (IOException e) {
			handleError(e);
		}
	}

	private void handleVote(Vote msg, ID containerID) {
		synchronized (elections) {
			Election election = (Election) elections.get(msg.getElectionID());
			if (election != null) {
				switch (election.processVote(msg.getTicket(), containerID)) {
				case Election.WON:
					processVictory(msg.getElectionID());
				case Election.LOST:
					elections.remove(msg.getElectionID());
				}
			}
		}
	}

	private void handleElected(Elected msg) {
		synchronized (elections) {
			elections.remove(msg.getElectionID());
		}
	}

	private void handleDeparted(ID containerID) {
		synchronized (elections) {
			for (Iterator i = elections.entrySet().iterator(); i.hasNext();) {
				Map.Entry entry = (Map.Entry) i.next();
				Election election = (Election) entry.getValue();
				switch (election.disqualify(containerID)) {
				case Election.WON:
					processVictory((ID) entry.getKey());
				case Election.LOST:
					i.remove();
					break;
				}
			}
		}
	}

	private void processVictory(ID electionID) {
		try {
			config.getContext().sendMessage(null, new Elected(electionID));
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
	 * @see org.eclipse.ecf.core.ISharedObject#handleEvents(org.eclipse.ecf.core.util.Event[])
	 */
	public void handleEvents(Event[] events) {
		for (int i = 0; i < events.length; ++i)
			handleEvent(events[i]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObject#dispose(org.eclipse.ecf.core.identity.ID)
	 */
	public synchronized void dispose(ID containerID) {
		config = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObject#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class clazz) {
		return null;
	}

	private class Election {

		public static final short LOST = 0;

		public static final short WON = 1;

		public static final short UNKNOWN = 2;

		private final long ticket;

		private final HashSet members;

		public Election(long ticket, ID[] members) {
			this.ticket = ticket;
			this.members = new HashSet(Arrays.asList(members));
		}

		public short processVote(long ticket, ID containerID) {
			if (this.ticket < ticket)
				return LOST;
			else {
				members.remove(containerID);
				return members.isEmpty() ? WON : UNKNOWN;
			}
		}

		public short disqualify(ID containerID) {
			members.remove(containerID);
			return members.isEmpty() ? WON : UNKNOWN;
		}
	}
}
