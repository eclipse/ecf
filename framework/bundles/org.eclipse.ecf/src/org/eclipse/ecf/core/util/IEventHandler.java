package org.eclipse.ecf.core.util;

public interface IEventHandler {

    /**
     * Handle Event passed to this ISharedObject. The ISharedObjectContainer
     * will pass events to all SharedObjects via this method and the
     * handleEvents method.
     * 
     * @param event
     *            the Event for the ISharedObject to process
     */
    public void handleEvent(Event event);

    /**
     * Handle Events passed to this ISharedObject. The ISharedObjectContainer
     * will pass events to all SharedObjects via this method and the
     * handleEvents method.
     * 
     * @param events
     *            the Events [] for the ISharedObject to process
     */
    public void handleEvents(Event[] events);

}
