/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.core.util;

public interface QueueEnqueue {
    void enqueue(Event element) throws QueueException;
    void enqueue(Event[] elements) throws QueueException;

    Object enqueue_prepare(Event[] elements) throws QueueException;

    void enqueue_commit(Object enqueue_key);
    void enqueue_abort(Object enqueue_key);

    boolean enqueue_lossy(Event element);

    void setEnqueuePredicate(EnqueuePredicate pred);

    EnqueuePredicate getEnqueuePredicate();

    int size();

}