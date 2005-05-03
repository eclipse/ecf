/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.core.identity;

import org.eclipse.ecf.core.util.ECFException;

public class IDInstantiationException extends ECFException {

	private static final long serialVersionUID = 3258416140119323960L;

	public IDInstantiationException() {
        super();
    }

    public IDInstantiationException(String message) {
        super(message);
    }

    public IDInstantiationException(Throwable cause) {
        super(cause);
    }

    public IDInstantiationException(String message, Throwable cause) {
        super(message, cause);
    }

}