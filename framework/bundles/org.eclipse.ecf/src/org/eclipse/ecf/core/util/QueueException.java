/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.core.util;

public class QueueException extends Exception {

    /**
     * 
     * @uml.property name="theQueue"
     * @uml.associationEnd
     * @uml.property name="theQueue" multiplicity="(0 1)"
     */
    Queue theQueue = null;

    public QueueException() {
        super();
    }

    public QueueException(Queue queue) {
        theQueue = queue;
    }
    public QueueException(String message) {
        super(message);
    }
    public QueueException(String message, Throwable cause) {
        super(message, cause);
    }
    public QueueException(Throwable cause) {
        super(cause);
    }
    public Queue getQueue() {
        return theQueue;
    }
}