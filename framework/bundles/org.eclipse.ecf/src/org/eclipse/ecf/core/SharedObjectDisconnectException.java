/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.core;

import org.eclipse.ecf.core.util.ECFException;

public class SharedObjectDisconnectException extends ECFException {

    public SharedObjectDisconnectException() {
        super();
    }

    public SharedObjectDisconnectException(String arg0) {
        super(arg0);
    }

    public SharedObjectDisconnectException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public SharedObjectDisconnectException(Throwable cause) {
        super(cause);
    }

}