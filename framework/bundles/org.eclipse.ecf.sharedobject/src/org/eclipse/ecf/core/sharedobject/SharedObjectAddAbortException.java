/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.core.sharedobject;

import java.util.List;
import java.util.Map;

/**
 * Exception thrown during transactional add of shared object
 * 
 * @see ISharedObjectContainerTransaction#waitToCommit()
 * 
 */
public class SharedObjectAddAbortException extends SharedObjectAddException {
	private static final long serialVersionUID = 4120851079287223088L;

	protected long timeout = -1L;

	protected Map causes;

	protected List participants;

	public SharedObjectAddAbortException() {
		super();
	}

	public SharedObjectAddAbortException(String arg0) {
		super(arg0);
	}

	public SharedObjectAddAbortException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public SharedObjectAddAbortException(String msg, Throwable cause,
			int timeout) {
		super(msg, cause);
		this.timeout = timeout;
	}

	public SharedObjectAddAbortException(String msg, Map causes, int timeout) {
		this(msg, null, causes, timeout);
	}

	public SharedObjectAddAbortException(String msg, List participants,
			Map causes, int timeout) {
		super(msg);
		this.participants = participants;
		this.causes = causes;
		this.timeout = timeout;
	}

	public SharedObjectAddAbortException(Throwable cause) {
		super(cause);
	}

	public long getTimeout() {
		return timeout;
	}

	public Map getCauses() {
		return causes;
	}
}