/****************************************************************************
* Copyright (c) 2004 Composent, Inc. and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Composent, Inc. - initial API and implementation
*****************************************************************************/

package org.eclipse.ecf.core.comm;

public class ConnectionInstantiationException extends Exception {

    public ConnectionInstantiationException() {
        super();
    }

    /**
     * @param message
     */
    public ConnectionInstantiationException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public ConnectionInstantiationException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public ConnectionInstantiationException(String message, Throwable cause) {
        super(message, cause);
    }

}