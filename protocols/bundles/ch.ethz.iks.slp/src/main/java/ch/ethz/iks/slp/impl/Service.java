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

import java.util.Dictionary;
import ch.ethz.iks.slp.ServiceURL;

/**
 * encapsulates the internal information about registered services.
 * 
 * @author Jan S. Rellermeyer, IKS, ETH Zurich
 * @since 0.6
 */
class Service {

	/**
	 * the service URL.
	 */
	ServiceURL url;

	/**
	 * the service attributes.
	 */
	Dictionary attributes;

	/**
	 * creates a new Service instance.
	 * 
	 * @param sreg
	 *            the service registration message.
	 */
	Service(final ServiceRegistration sreg) {

		// TODO: support localized registrations ...
		url = sreg.url;
		attributes = SLPUtils.attrListToDict(sreg.attList);

	}

	/**
	 * @param obj
	 *            Object to compare.
	 * @return <code>true</code> if the object is of type <code>Service</code>
	 *         and the two services have a matching serviceURL and equal
	 *         properties.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(final Object obj) {
		if (obj instanceof Service) {
			Service service = (Service) obj;
			return attributes.equals(service.attributes)
					&& url.equals(service.url);
		}
		return false;
	}

	/**
	 * get the hash code.
	 * 
	 * @return the hash code.
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return url.hashCode();
	}

	/**
	 * get a string representation.
	 * 
	 * @return a string.
	 */
	public String toString() {
		return url.toString();
	}
}
