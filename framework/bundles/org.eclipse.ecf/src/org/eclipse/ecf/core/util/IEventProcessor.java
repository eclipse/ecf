/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.core.util;

public interface IEventProcessor {
    /**
     * Returns true if given Event will actually be processed by this event processor, false if not
     * 
     * @param e the event to check
     * @return true if given Event will actually be processed by this event processor, false if not
     */
    public boolean willProcess(Event e);
    /**
     * Process given Event
     * @param e the Event to process
     */
    public void process(Event e);
    /**
     * Dispose this event processor.  Once disposed, given processor cannot be reused
     *
     */
    public void dispose();
}