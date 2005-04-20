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
public class Elected implements Serializable {

	private static final long serialVersionUID = 3258130271390937656L;

	private final ID electionID;

	public Elected(ID electionID) {
		this.electionID = electionID;
	}

	public ID getElectionID() {
		return electionID;
	}
}
