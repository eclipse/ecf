/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.core;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.Event;

public interface ISharedObject {

    /*
     * Initialize this ISharedObject. The ISharedObjectContainer for this
     * ISharedObject must call this method with a non-null instance of
     * ISharedObjectConfig. ISharedObject implementations can use this
     * initialization to perform any initialization necessary prior to receiving
     * any events (via handleEvent/s). Note that the ISharedObjectContext
     * provided via the ISharedObjectConfig.getSharedObjectContext() method is
     * *not* guaranteed to allow any method calls until after this init() method
     * call has completed.
     * 
     * @param initData the initialization data passed by the
     * ISharedObjectContainer upon initialization @exception
     * SharedObjectInitException thrown by ISharedObject to halt initialization
     * prematurely. ISharedObjectContainer may respond to such an exception by
     * halt any further processing by ISharedObject
     */
    public void init(ISharedObjectConfig initData)
            throws SharedObjectInitException;

    /*
     * Handle Event passed to this ISharedObject. The ISharedObjectContainer
     * will pass events to all SharedObjects via this method and the
     * handleEvents method.
     * 
     * @param event the Event for the ISharedObject to process
     */
    public void handleEvent(Event event);

    /*
     * Handle Events passed to this ISharedObject. The ISharedObjectContainer
     * will pass events to all SharedObjects via this method and the
     * handleEvents method.
     * 
     * @param event the Events [] for the ISharedObject to process
     */
    public void handleEvents(Event[] events);

    /*
     * Method called by the ISharedObjectContainer upon ISharedObject
     * destruction. Once this method is called, no more Events will be passed to
     * a ISharedObject until the init method is called again.
     */
    public void dispose(ID containerID);

    /**
     * Provide access to an adapter object. This method guarantees that any
     * non-null object instance provided is an instance of the class provided as
     * the first parameter.
     * 
     * @param clazz
     *            the Class of the adapter. The returned Object instance must
     *            implement the given clazz
     * @returns Object the adaptor object
     */
    public Object getAdapter(Class clazz);
}