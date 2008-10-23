/* Copyright (c) 2005-2008 Jan S. Rellermeyer
 * Systems Group,
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ch.ethz.iks.slp.impl.filter.Filter;
import ch.ethz.iks.slp.impl.filter.RFC1960Filter;

/**
 * Platform abstraction for the standalone implementation.
 * 
 * @author Jan S. Rellermeyer, ETH Zurich
 */
public class StandalonePlatformAbstraction implements PlatformAbstraction {

	/**
	 * 
	 */
	private final Log log;

	/**
	 * 
	 */
	public StandalonePlatformAbstraction() {
		log = LogFactory.getLog(SLPCore.class);
	}

	/**
	 * 
	 * @see ch.ethz.iks.slp.impl.PlatformAbstraction#createFilter(java.lang.String)
	 */
	public Filter createFilter(String filterString)
			throws IllegalArgumentException {
		return RFC1960Filter.fromString(filterString);
	}

	/**
	 * 
	 * @see ch.ethz.iks.slp.impl.PlatformAbstraction#logDebug(java.lang.String)
	 */
	public void logDebug(final String message) {
		log.debug(message);
	}

	/**
	 * 
	 * @see ch.ethz.iks.slp.impl.PlatformAbstraction#logDebug(java.lang.String,
	 *      java.lang.Throwable)
	 */
	public void logDebug(final String message, final Throwable exception) {
		log.debug(message, exception);
	}

	/**
	 * 
	 * @see ch.ethz.iks.slp.impl.PlatformAbstraction#logError(java.lang.String)
	 */
	public void logError(final String message) {
		log.error(message);
	}

	/**
	 * 
	 * @see ch.ethz.iks.slp.impl.PlatformAbstraction#logError(java.lang.String,
	 *      java.lang.Throwable)
	 */
	public void logError(final String message, final Throwable exception) {
		log.error(message, exception);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.slp.impl.PlatformAbstraction#logTraceDrop(java.lang.String)
	 */
	public void logTraceDrop(String string) {
		if(SLPCore.CONFIG.getTraceDrop()) {
			log.trace(string);
		}
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.slp.impl.PlatformAbstraction#logTraceMessage(java.lang.String)
	 */
	public void logTraceMessage(String string) {
		if(SLPCore.CONFIG.getTraceMessage()) {
			log.trace(string);
		}
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.slp.impl.PlatformAbstraction#logTraceReg(java.lang.String)
	 */
	public void logTraceReg(String string) {
		if(SLPCore.CONFIG.getTraceReg()) {
			log.trace(string);
		}
	}

	/**
	 * 
	 * @see ch.ethz.iks.slp.impl.PlatformAbstraction#logWarning(java.lang.String)
	 */
	public void logWarning(final String message) {
		log.warn(message);
	}

	/**
	 * 
	 * @see ch.ethz.iks.slp.impl.PlatformAbstraction#logWarning(java.lang.String,
	 *      java.lang.Throwable)
	 */
	public void logWarning(final String message, final Throwable exception) {
		log.warn(message, exception);
	}
}
