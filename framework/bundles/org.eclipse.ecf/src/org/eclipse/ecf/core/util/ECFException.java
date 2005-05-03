/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.core.util;

public class ECFException extends Exception {

	private static final long serialVersionUID = 3256440309134406707L;

	public ECFException() {
        super();
    }

    /**
     * @param message
     */
    public ECFException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public ECFException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public ECFException(String message, Throwable cause) {
        super(message, cause);
    }

}