/* Copyright (c) 2005-2007 Jan S. Rellermeyer
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
package ch.ethz.iks.slp.impl;

import ch.ethz.iks.slp.impl.filter.Filter;

/**
 * Platform abstraction interface. Used to hide the different implementations
 * for the OSGi platform and for stand-alone Java.
 * 
 * @author Jan S. Rellermeyer, ETH Zurich.
 */
public interface PlatformAbstraction {

	/**
	 * Write a debug message to the log.
	 * 
	 * @param message
	 *            the message.
	 */
	void logDebug(String message);

	/**
	 * Write a debug message to the log.
	 * 
	 * @param message
	 *            the message.
	 * @param exception
	 *            an exception.
	 */
	void logDebug(String message, Throwable exception);

	/**
	 * Trace a generic message to the log.
	 * 
	 * @param message
	 *            the message.
	 */
	void logTraceMessage(String string);

	/**
	 * Trace a registration to the log.
	 * 
	 * @param message
	 *            the message.
	 */
	void logTraceReg(String string);

	/**
	 * Trace a drop to the log.
	 * 
	 * @param message
	 *            the message.
	 */
	void logTraceDrop(String string);

	/**
	 * Write a warning message to the log.
	 * 
	 * @param message
	 *            the message.
	 */
	void logWarning(String message);

	/**
	 * Write a warning message to the log.
	 * 
	 * @param message
	 *            the message.
	 * @param exception
	 *            an exception.
	 */
	void logWarning(String message, Throwable exception);

	/**
	 * Write an error message to the log.
	 * 
	 * @param message
	 *            the message.
	 */
	void logError(String message);

	/**
	 * Write an error message to the log.
	 * 
	 * @param message
	 *            the message.
	 * @param exception
	 *            an exception.
	 */
	void logError(String message, Throwable exception);

	/**
	 * Create an LDAP filter.
	 * 
	 * @param filterString
	 *            the filter string.
	 * @return an LDAP filter object.
	 */
	Filter createFilter(String filterString);
}
