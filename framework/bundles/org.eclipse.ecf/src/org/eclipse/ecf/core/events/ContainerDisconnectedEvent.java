/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.events;

import org.eclipse.ecf.core.identity.ID;

public class ContainerDisconnectedEvent implements
		IContainerDisconnectedEvent {
	private static final long serialVersionUID = 3256437002059527733L;
	private final ID departedContainerID;
	private final ID localContainerID;
	private Throwable exception = null;
	
	public ContainerDisconnectedEvent(ID container, ID o) {
		this(container,o,null);
	}

	public ContainerDisconnectedEvent(ID container, ID o, Throwable t) {
		this.localContainerID = container;
		this.departedContainerID = o;
		this.exception = t;
	}
	public ID getTargetID() {
		return departedContainerID;
	}

	public ID getLocalContainerID() {
		return localContainerID;
	}

	public Throwable getException() {
		return exception;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer(
				"ContainerDisconnectedEvent[");
		buf.append(getTargetID()).append(";");
		buf.append(getLocalContainerID()).append(";");
		buf.append(getException()).append("]");
		return buf.toString();
	}
}