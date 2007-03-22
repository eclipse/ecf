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

package org.eclipse.ecf.remoteservice.util;

import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.osgi.framework.Filter;

public class RemoteServiceTracker implements IRemoteServiceTrackerCustomizer {

	/* set this to true to compile in debug messages */
	static final boolean				DEBUG			= false;
	/**
	 * Container adapter against which this <code>RemoteServiceTracker</code> object is
	 * tracking.
	 */
	//protected final IRemoteServiceContainerAdapter		context;
	/**
	 * Filter specifying search criteria for the services to track.
	 * 
	 */
	//protected final Filter				filter;
	/**
	 * <code>IRemoteServiceTrackerCustomizer</code> object for this tracker.
	 */
	//final IRemoteServiceTrackerCustomizer		customizer;
	/**
	 * Filter string for use when adding the ServiceListener.
	 */
	//private final String				listenerFilter;
	/**
	 * Class name to be tracked. If this field is set, then we are tracking by
	 * class name.
	 */
	//private final String				trackClass;
	/**
	 * Reference to be tracked. If this field is set, then we are tracking a
	 * single IRemoteServiceReference.
	 */
	//private final IRemoteServiceReference		trackReference;
	/**
	 * True if no Filter object was supplied in a constructor or we are not
	 * using the supplied filter.
	 */
	//final boolean 						noUserFilter;
	/**
	 * Tracked services: <code>ServiceReference</code> object -> customized
	 * Object and <code>ServiceListener</code> object
	 */
	//private volatile Tracked			tracked;
	/**
	 * Modification count. This field is initialized to zero by open, set to -1
	 * by close and incremented by modified.
	 * 
	 * This field is volatile since it is accessed by multiple threads.
	 */
	private volatile int				trackingCount	= -1;
	/**
	 * Cached ServiceReference for getServiceReference.
	 * 
	 * This field is volatile since it is accessed by multiple threads.
	 */
	private volatile IRemoteServiceReference	cachedReference;
	/**
	 * Cached service object for getService.
	 * 
	 * This field is volatile since it is accessed by multiple threads.
	 */
	private volatile Object				cachedService;

	/**
	 * Inner class to track services. If a <code>ServiceTracker</code> object
	 * is reused (closed then reopened), then a new Tracked object is used. This
	 * class is a hashtable mapping <code>ServiceReference</code> object ->
	 * customized Object. This class is the <code>ServiceListener</code>
	 * object for the tracker. This class is used to synchronize access to the
	 * tracked services. This is not a public class. It is only for use by the
	 * implementation of the <code>ServiceTracker</code> class.
	 * 
	 * @ThreadSafe
	 */
	
/*	
	class Tracked extends Hashtable implements ServiceListener {
		static final long			serialVersionUID	= -7420065199791006079L;
		*//**
		 * List of ServiceReferences in the process of being added. This is used
		 * to deal with nesting of ServiceEvents. Since ServiceEvents are
		 * synchronously delivered, ServiceEvents can be nested. For example,
		 * when processing the adding of a service and the customizer causes the
		 * service to be unregistered, notification to the nested call to
		 * untrack that the service was unregistered can be made to the track
		 * method.
		 * 
		 * Since the ArrayList implementation is not synchronized, all access to
		 * this list must be protected by the same synchronized object for
		 * thread-safety.
		 * 
		 * @GuardedBy this
		 *//*
		private final ArrayList		adding;

		*//**
		 * true if the tracked object is closed.
		 * 
		 * This field is volatile because it is set by one thread and read by
		 * another.
		 *//*
		private volatile boolean	closed;

		*//**
		 * Initial list of ServiceReferences for the tracker. This is used to
		 * correctly process the initial services which could become
		 * unregistered before they are tracked. This is necessary since the
		 * initial set of tracked services are not "announced" by ServiceEvents
		 * and therefore the ServiceEvent for unregistration could be delivered
		 * before we track the service.
		 * 
		 * A service must not be in both the initial and adding lists at the
		 * same time. A service must be moved from the initial list to the
		 * adding list "atomically" before we begin tracking it.
		 * 
		 * Since the LinkedList implementation is not synchronized, all access
		 * to this list must be protected by the same synchronized object for
		 * thread-safety.
		 * 
		 * @GuardedBy this
		 *//*
		private final LinkedList	initial;

		*//**
		 * Tracked constructor.
		 *//*
		protected Tracked() {
			super();
			closed = false;
			adding = new ArrayList(6);
			initial = new LinkedList();
		}

		*//**
		 * Set initial list of services into tracker before ServiceEvents begin
		 * to be received.
		 * 
		 * This method must be called from ServiceTracker.open while
		 * synchronized on this object in the same synchronized block as the
		 * addServiceListener call.
		 * 
		 * @param references The initial list of services to be tracked.
		 * @GuardedBy this
		 *//*
		protected void setInitialServices(ServiceReference[] references) {
			if (references == null) {
				return;
			}
			int size = references.length;
			for (int i = 0; i < size; i++) {
				if (DEBUG) {
					System.out
							.println("ServiceTracker.Tracked.setInitialServices: " + references[i]); //$NON-NLS-1$
				}
				initial.add(references[i]);
			}
		}

		*//**
		 * Track the initial list of services. This is called after
		 * ServiceEvents can begin to be received.
		 * 
		 * This method must be called from ServiceTracker.open while not
		 * synchronized on this object after the addServiceListener call.
		 * 
		 *//*
		protected void trackInitialServices() {
			while (true) {
				ServiceReference reference;
				synchronized (this) {
					if (initial.size() == 0) {
						
						 * if there are no more inital services
						 
						return;  we are done 
					}
					
					 * move the first service from the initial list to the
					 * adding list within this synchronized block.
					 
					reference = (ServiceReference) initial.removeFirst();
					if (this.get(reference) != null) {
						 if we are already tracking this service 
						if (DEBUG) {
							System.out
									.println("ServiceTracker.Tracked.trackInitialServices[already tracked]: " + reference); //$NON-NLS-1$
						}
						continue;  skip this service 
					}
					if (adding.contains(reference)) {
						
						 * if this service is already in the process of being
						 * added.
						 
						if (DEBUG) {
							System.out
									.println("ServiceTracker.Tracked.trackInitialServices[already adding]: " + reference); //$NON-NLS-1$
						}
						continue;  skip this service 
					}
					adding.add(reference);
				}
				if (DEBUG) {
					System.out
							.println("ServiceTracker.Tracked.trackInitialServices: " + reference); //$NON-NLS-1$
				}
				trackAdding(reference); 
										 * Begin tracking it. We call
										 * trackAdding since we have already put
										 * the reference in the adding list.
										 
			}
		}

		*//**
		 * Called by the owning <code>ServiceTracker</code> object when it is
		 * closed.
		 *//*
		protected void close() {
			closed = true;
		}

		*//**
		 * <code>ServiceListener</code> method for the
		 * <code>ServiceTracker</code> class. This method must NOT be
		 * synchronized to avoid deadlock potential.
		 * 
		 * @param event <code>ServiceEvent</code> object from the framework.
		 *//*
		public void serviceChanged(ServiceEvent event) {
			
			 * Check if we had a delayed call (which could happen when we
			 * close).
			 
			if (closed) {
				return;
			}
			ServiceReference reference = event.getServiceReference();
			if (DEBUG) {
				System.out
						.println("ServiceTracker.Tracked.serviceChanged[" + event.getType() + "]: " + reference); //$NON-NLS-1$ //$NON-NLS-2$
			}

			switch (event.getType()) {
				case ServiceEvent.REGISTERED :
				case ServiceEvent.MODIFIED :
					if (noUserFilter) { // no user supplied filter to be checked
						track(reference);
						
						 * If the customizer throws an unchecked exception, it
						 * is safe to let it propagate
						 
					}
					else { // filter supplied by user must be checked
						if (filter.match(reference)) {
							track(reference);
							
							 * If the customizer throws an unchecked exception,
							 * it is safe to let it propagate
							 
						}
						else {
							untrack(reference);
							
							 * If the customizer throws an unchecked exception,
							 * it is safe to let it propagate
							 
						}
					}
					break;
				case ServiceEvent.UNREGISTERING :
					untrack(reference);
					
					 * If the customizer throws an unchecked exception, it is
					 * safe to let it propagate
					 
					break;
			}
		}

		*//**
		 * Begin to track the referenced service.
		 * 
		 * @param reference Reference to a service to be tracked.
		 *//*
		private void track(ServiceReference reference) {
			Object object;
			synchronized (this) {
				object = this.get(reference);
			}
			if (object != null)  we are already tracking the service 
			{
				if (DEBUG) {
					System.out
							.println("ServiceTracker.Tracked.track[modified]: " + reference); //$NON-NLS-1$
				}
				synchronized (this) {
					modified();  increment modification count 
				}
				 Call customizer outside of synchronized region 
				customizer.modifiedService(reference, object);
				
				 * If the customizer throws an unchecked exception, it is safe
				 * to let it propagate
				 
				return;
			}
			synchronized (this) {
				if (adding.contains(reference)) { 
													 * if this service is
													 * already in the process of
													 * being added.
													 
					if (DEBUG) {
						System.out
								.println("ServiceTracker.Tracked.track[already adding]: " + reference); //$NON-NLS-1$
					}
					return;
				}
				adding.add(reference);  mark this service is being added 
			}

			trackAdding(reference); 
									 * call trackAdding now that we have put the
									 * reference in the adding list
									 
		}

		*//**
		 * Common logic to add a service to the tracker used by track and
		 * trackInitialServices. The specified reference must have been placed
		 * in the adding list before calling this method.
		 * 
		 * @param reference Reference to a service to be tracked.
		 *//*
		private void trackAdding(ServiceReference reference) {
			if (DEBUG) {
				System.out
						.println("ServiceTracker.Tracked.trackAdding: " + reference); //$NON-NLS-1$
			}
			Object object = null;
			boolean becameUntracked = false;
			 Call customizer outside of synchronized region 
			try {
				object = customizer.addingService(reference);
				
				 * If the customizer throws an unchecked exception, it will
				 * propagate after the finally
				 
			}
			finally {
				synchronized (this) {
					if (adding.remove(reference)) { 
													 * if the service was not
													 * untracked during the
													 * customizer callback
													 
						if (object != null) {
							this.put(reference, object);
							modified();  increment modification count 
							notifyAll(); 
											 * notify any waiters in
											 * waitForService
											 
						}
					}
					else {
						becameUntracked = true;
					}
				}
			}
			
			 * The service became untracked during the customizer callback.
			 
			if (becameUntracked) {
				if (DEBUG) {
					System.out
							.println("ServiceTracker.Tracked.trackAdding[removed]: " + reference); //$NON-NLS-1$
				}
				 Call customizer outside of synchronized region 
				customizer.removedService(reference, object);
				
				 * If the customizer throws an unchecked exception, it is safe
				 * to let it propagate
				 
			}
		}

		*//**
		 * Discontinue tracking the referenced service.
		 * 
		 * @param reference Reference to the tracked service.
		 *//*
		protected void untrack(ServiceReference reference) {
			Object object;
			synchronized (this) {
				if (initial.remove(reference)) { 
													 * if this service is
													 * already in the list of
													 * initial references to
													 * process
													 
					if (DEBUG) {
						System.out
								.println("ServiceTracker.Tracked.untrack[removed from initial]: " + reference); //$NON-NLS-1$
					}
					return; 
							 * we have removed it from the list and it will not
							 * be processed
							 
				}

				if (adding.remove(reference)) { 
												 * if the service is in the
												 * process of being added
												 
					if (DEBUG) {
						System.out
								.println("ServiceTracker.Tracked.untrack[being added]: " + reference); //$NON-NLS-1$
					}
					return; 
							 * in case the service is untracked while in the
							 * process of adding
							 
				}
				object = this.remove(reference); 
													 * must remove from tracker
													 * before calling customizer
													 * callback
													 
				if (object == null) {  are we actually tracking the service 
					return;
				}
				modified();  increment modification count 
			}
			if (DEBUG) {
				System.out
						.println("ServiceTracker.Tracked.untrack[removed]: " + reference); //$NON-NLS-1$
			}
			 Call customizer outside of synchronized region 
			customizer.removedService(reference, object);
			
			 * If the customizer throws an unchecked exception, it is safe to
			 * let it propagate
			 
		}
	}

*/	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.remoteservice.util.IRemoteServiceTrackerCustomizer#addingService(org.eclipse.ecf.remoteservice.IRemoteServiceReference)
	 */
	public Object addingService(IRemoteServiceReference reference) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.remoteservice.util.IRemoteServiceTrackerCustomizer#modifiedService(org.eclipse.ecf.remoteservice.IRemoteServiceReference, java.lang.Object)
	 */
	public void modifiedService(IRemoteServiceReference reference,
			Object service) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.remoteservice.util.IRemoteServiceTrackerCustomizer#removedService(org.eclipse.ecf.remoteservice.IRemoteServiceReference, java.lang.Object)
	 */
	public void removedService(IRemoteServiceReference reference, Object service) {
		// TODO Auto-generated method stub

	}

}
