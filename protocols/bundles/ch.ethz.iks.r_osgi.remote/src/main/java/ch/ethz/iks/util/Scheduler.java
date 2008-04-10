/* Copyright (c) 2006-2008 Jan S. Rellermeyer
 * Information and Communication Systems Research Group (IKS),
 * Department of Computer Science, ETH Zurich.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    - Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    - Neither the name of ETH Zurich nor the names of its contributors may be
 *      used to endorse or promote products derived from this software without
 *      specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package ch.ethz.iks.util;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Scheduler utility. Objects can be scheduled and the registered listener is
 * called when the scheduled object becomes due.
 * 
 * @author Jan S. Rellermeyer, ETH Zurich.
 * @since 0.6
 */
public final class Scheduler {

	/**
	 * the expiration queue. All scheduled objects are places in ascending order
	 * of their timestamp.
	 */
	private SortedMap expirationQueue = new TreeMap();

	/**
	 * data index of the expiration queue to lookup for which timestamp an
	 * object is scheduled.
	 */
	private Map expirationDataIndex = new HashMap(0);

	/**
	 * the listener that is called whenever a scheduled object has become due.
	 */
	private ScheduleListener listener;

	/**
	 * the schedule thread.
	 */
	private ScheduleThread thread;

	/**
	 * the thread variable.
	 */
	private boolean running;

	/**
	 * create a new scheduler.
	 * 
	 * @param listener
	 *            the listener that is called whenever a scheduled object has
	 *            become due.
	 */
	public Scheduler(final ScheduleListener listener) {
		this.listener = listener;
		this.thread = new ScheduleThread();
		start();
	}

	/**
	 * start the scheduler.
	 */
	public void start() {
		running = true;
		thread.start();
	}

	/**
	 * stop the scheduler.
	 */
	public void stop() {
		running = false;
		synchronized (expirationQueue) {
			expirationQueue.clear();
			expirationDataIndex.clear();
			expirationQueue.notifyAll();
		}
	}

	/**
	 * check, if an object is already scheduled.
	 * 
	 * @param object
	 *            the object to check.
	 * @return true, if the object is already scheduled.
	 */
	public boolean isScheduled(final Object object) {
		synchronized (expirationQueue) {
			Long scheduled = (Long) expirationDataIndex.get(object);
			return (scheduled != null);
		}
	}

	/**
	 * schedule an object.
	 * 
	 * @param object
	 *            the object.
	 * @param timestamp
	 *            the timestamp.
	 * @throws IllegalStateException
	 *             if the object is already scheduled with a different
	 *             timestamp.
	 */
	public void schedule(final Object object, final long timestamp) throws IllegalStateException {
		synchronized (expirationQueue) {
			if (isScheduled(object)) {
				throw new IllegalStateException("Object " + object + " is already scheduled."); //$NON-NLS-1$ //$NON-NLS-2$
			}
			Long ts = new Long(timestamp);
			expirationQueue.put(ts, object);
			expirationDataIndex.put(object, ts);
			expirationQueue.notifyAll();
		}
	}

	/**
	 * reschedule an object for a new timestamp.
	 * 
	 * @param object
	 *            the object.
	 * @param newTimestamp
	 *            the new timestamp.
	 */
	public void reschedule(final Object object, final long newTimestamp) {
		synchronized (expirationQueue) {
			unschedule(object);
			schedule(object, newTimestamp);
		}
	}

	/**
	 * unschedule an object.
	 * 
	 * @param object
	 *            the object.
	 */
	public void unschedule(final Object object) {
		synchronized (expirationQueue) {
			Long scheduled = (Long) expirationDataIndex.remove(object);
			expirationQueue.remove(scheduled);
		}
	}

	/**
	 * the scheduler thread.
	 * 
	 * @author Jan S. Rellermeyer, ETH Zurich
	 */
	private class ScheduleThread extends Thread {

		/**
		 * create a new schedule thread.
		 */
		ScheduleThread() {
			setDaemon(true);
			setName("Scheduler"); //$NON-NLS-1$
		}

		/**
		 * the thread's main loop.
		 */
		public void run() {
			try {
				while (running) {
					synchronized (expirationQueue) {
						if (expirationQueue.isEmpty()) {
							// nothing to do, sleep until something arrives
							expirationQueue.wait();
						} else {
							// we have work, do everything that is due
							Long nextActivity;
							while (!expirationQueue.isEmpty() && (nextActivity = ((Long) expirationQueue.firstKey())).longValue() <= System.currentTimeMillis() + 10) {
								final Object object = expirationQueue.remove(nextActivity);
								listener.due(Scheduler.this, nextActivity.longValue(), object);
							}
							if (!expirationQueue.isEmpty()) {
								nextActivity = ((Long) expirationQueue.firstKey());
								final long next = nextActivity.longValue() - System.currentTimeMillis() - 10;
								/*
								 * there are some activities in the future,
								 * sleep until the first activity becomes due
								 */
								if (next > 0) {
									expirationQueue.wait(next);
								}
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
