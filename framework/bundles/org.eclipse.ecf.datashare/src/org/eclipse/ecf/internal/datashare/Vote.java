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

import java.io.Serializable;

import org.eclipse.ecf.core.identity.ID;

/**
 * @author pnehrer
 */
public class Vote implements Serializable {

	private static final long serialVersionUID = 3977585813699507248L;

	private final long ticket;

	private final ID electionID;

	public Vote(long ticket, ID electionID) {
		this.ticket = ticket;
		this.electionID = electionID;
	}

	public long getTicket() {
		return ticket;
	}

	public ID getElectionID() {
		return electionID;
	}
}
