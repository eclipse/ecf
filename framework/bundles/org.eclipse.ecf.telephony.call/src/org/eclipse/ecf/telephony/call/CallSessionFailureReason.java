/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.telephony.call;

/**
 * A class to represent the reason for a failure.  See {@link ICallSession#getErrorDetails()}.
 */
public class CallSessionFailureReason {

	protected int code = -1;
	protected String reason;

	public CallSessionFailureReason(int code, String reason) {
		this.code = code;
		this.reason = reason;
	}

	public CallSessionFailureReason(int code) {
		this(code, String.valueOf(code));
	}

	public String getReason() {
		return reason;
	}

	public int getCode() {
		return code;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer("CallSessionFailureReason["); //$NON-NLS-1$
		buffer.append("code=").append(code); //$NON-NLS-1$
		buffer.append(";reason=").append(reason).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
		return buffer.toString();
	}
}
