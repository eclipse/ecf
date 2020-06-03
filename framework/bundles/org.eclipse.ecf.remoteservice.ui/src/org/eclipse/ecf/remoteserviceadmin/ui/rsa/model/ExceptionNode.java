/****************************************************************************
 * Copyright (c) 2015 Composent, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Scott Lewis - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.remoteserviceadmin.ui.rsa.model;

/**
 * @since 3.4
 */
public class ExceptionNode extends NameValuePropertyNode {

	public ExceptionNode(String exceptionLabel, Throwable exception, boolean showStack) {
		super(exceptionLabel, exception.getLocalizedMessage());
		if (showStack) {
			for (StackTraceElement ste : exception.getStackTrace())
				addChild(new StackTraceElementNode(ste.toString()));
			Throwable cause = exception.getCause();
			if (cause != null)
				addChild(new ExceptionNode(cause, true));
		}
	}

	public ExceptionNode(Throwable exception, boolean showStack) {
		this("", exception, showStack);
		setNameValueSeparator("");
	}
}
