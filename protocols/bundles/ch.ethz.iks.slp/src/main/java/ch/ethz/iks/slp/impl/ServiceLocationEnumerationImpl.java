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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.ethz.iks.slp.ServiceLocationEnumeration;
import ch.ethz.iks.slp.ServiceLocationException;

/**
 * the implementation of a ServiceLocationEnumeration.
 * 
 * @see ch.ethz.iks.slp.ServiceLocationEnumeration
 * @author Jan S. Rellermeyer, IKS, ETH Zürich
 * @since 0.1
 */
class ServiceLocationEnumerationImpl implements ServiceLocationEnumeration {
	/**
	 * a list of results.
	 */
	private List list;

	/**
	 * internal Iterator over the elements of the list.
	 */
	private Iterator iterator;

	/**
	 * creates a new ServiceLocationEnumerationImpl.
	 * 
	 * @param resultList
	 *            a list of results.
	 */
	ServiceLocationEnumerationImpl(final List resultList) {
		list = resultList != null ? resultList : new ArrayList();
		this.iterator = list.iterator();
	}

	/**
	 * returns the next element of the Enumeration.
	 * 
	 * @return the next element.
	 * @throws ServiceLocationException
	 *             if there is no more element.
	 * @see ch.ethz.iks.slp.ServiceLocationEnumeration#next()
	 */
	public synchronized Object next() throws ServiceLocationException {
		try {
			return iterator.next();
		} catch (Exception e) {
			throw new ServiceLocationException(
					ServiceLocationException.INTERNAL_SYSTEM_ERROR, e
							.getMessage());
		}
	}

	/**
	 * checks if the Enumeration has more elements.
	 * 
	 * @return true if there are more elements available.
	 */
	public synchronized boolean hasMoreElements() {
		return iterator.hasNext();
	}

	/**
	 * returns the next elenemt of the Enumeration.
	 * 
	 * @return the next element or null if there aren't any more.
	 */
	public synchronized Object nextElement() {
		try {
			return next();
		} catch (ServiceLocationException sle) {
			return null;
		}
	}
}
