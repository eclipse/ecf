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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.ecf.core.ISharedObjectConfig;
import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.events.ISharedObjectContainerDepartedEvent;
import org.eclipse.ecf.core.events.ISharedObjectContainerJoinedEvent;
import org.eclipse.ecf.core.events.ISharedObjectMessageEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.Event;

/**
 * @author pnehrer
 */
public class LazyElectionBootstrap implements IBootstrap {

	private final Random random = new Random();

	private Agent agent;

	private ISharedObjectConfig config;

	private final HashMap elections = new HashMap();

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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.internal.datashare.IBootstrap#handleEvent(org.eclipse.ecf.core.util.Event)
	 */
	public void handleEvent(Event event) {
		if (event instanceof ISharedObjectContainerJoinedEvent) {
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
				handleElected((Elected) e.getData());
		}
	}

	private void handleJoined(ID containerID) {
		List members = Arrays.asList(config.getContext().getGroupMemberIDs());
		members.remove(containerID);
		members.remove(config.getContext().getLocalContainerID());
		if (members.isEmpty())
			processVictory(containerID);
		else {
			long ticket = random.nextLong(); // TODO strategize this
			Election election = new Election(ticket, members);
			synchronized (elections) {
				elections.put(containerID, election);
			}

			try {
				config.getContext().sendMessage(null,
						new Vote(ticket, containerID));
			} catch (IOException e) {
				handleError(e);
			}
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
				ID electionID = (ID) entry.getKey();
				if (containerID.equals(electionID))
					i.remove();
				else {
					Election election = (Election) entry.getValue();
					switch (election.disqualify(containerID)) {
					case Election.WON:
						processVictory(electionID);
					case Election.LOST:
						i.remove();
						break;
					}
				}
			}
		}
	}

	private void processVictory(ID electionID) {
		try {
			config.getContext().sendMessage(null, new Elected(electionID));
			agent.doBootstrap(electionID);
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
		config = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.internal.datashare.IBootstrap#createMemento()
	 */
	public IBootstrapMemento createMemento() {
		return new BootstrapMemento();
	}

	public static class BootstrapMemento implements IBootstrapMemento {

		private static final long serialVersionUID = 3256438127341418808L;

		public IBootstrap createBootstrap() {
			return new LazyElectionBootstrap();
		}
	}
}
