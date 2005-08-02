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

public class ContainerJoinException extends ECFException {

	private static final long serialVersionUID = 4078658849424746859L;

	public ContainerJoinException() {
        super();
    }

    /**
     * @param message
     */
    public ContainerJoinException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public ContainerJoinException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public ContainerJoinException(String message, Throwable cause) {
        super(message, cause);
    }

}