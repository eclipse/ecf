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
package ch.ethz.iks.slp;

import java.net.InetAddress;
import java.util.Dictionary;
import java.util.List;
import java.util.Locale;

/**
 * Advertiser implements the SA properties of SLP. Services can be registered
 * and deregistered. The SLP framework handles DA discovery.
 * 
 * @author Jan S. Rellermeyer, IKS, ETH Zurich
 * @since 0.1
 */
public interface Advertiser {

	/**
	 * Returns the locale of this Advertiser instance.
	 * 
	 * @return the current Locale.
	 */
	Locale getLocale();

	/**
	 * Get the locale of this instance.
	 * 
	 * @param locale
	 *            the Locale.
	 * @see Advertiser#getLocale()
	 */
	void setLocale(final Locale locale);

	/**
	 * Register a service with the SLP framework. The service will be registered
	 * with all known DAs that support the default scope and with the local SA
	 * registry for multicast discovery.
	 * 
	 * @param url
	 *            the <code>ServiceURL</code> of the service.
	 * @param attributes
	 *            a <code>Dictionary</code> of attributes for the service. RFC
	 *            2614 proposes a <code>Vector</code> of attribute-value-pairs
	 *            here but the <code>Dictionary</code> makes this
	 *            implementation more close to <code>OSGi</code>.
	 * @throws ServiceLocationException
	 *             in case that the registration failed for any reason.
	 */
	void register(ServiceURL url, Dictionary attributes)
			throws ServiceLocationException;

	/**
	 * Register a service with the SLP framework. The service will be registered
	 * with all known DAs that support at least one of the given scopes and with
	 * the local SA registry for multicast discovery.
	 * 
	 * @param url
	 *            the ServiceURL of the service.
	 * @param scopes
	 *            a <code>List</code> of scope names as <code>Strings</code>.
	 * @param attributes
	 *            a <code>Dictionary</code> of attributes for the service. RFC
	 *            2614 proposes a <code>Vector</code> of attribute-value-pairs
	 *            here but the <code>Dictionary</code> makes this
	 *            implementation more close to <code>OSGi</code>
	 * @throws ServiceLocationException
	 *             in case that the registration failed for any reason.
	 */
	void register(ServiceURL url, List scopes, Dictionary attributes)
			throws ServiceLocationException;

	/**
	 * Unregister a service with the SLP framework. The service will be
	 * unregistered with all known DAs in the scopes that it was registered in.
	 * 
	 * @param url
	 *            the <code>ServiceURL</code> of the service.
	 * @throws ServiceLocationException
	 *             in case that the deregistration failed for any reason.
	 */
	void deregister(ServiceURL url) throws ServiceLocationException;

	/**
	 * deregister a service in some scopes.
	 * 
	 * @param url
	 *            the ServiceURL of the service.
	 * @param scopes
	 *            the scopes.
	 * @throws ServiceLocationException
	 *             if the deregistration has failed for any reason.
	 * @see Advertiser#deregister(ServiceURL, List)
	 * @since 0.7.1
	 */
	void deregister(final ServiceURL url, final List scopes)
			throws ServiceLocationException;

	/**
	 * <b>Not yet implemented.</b> Add attributes to an already registered
	 * service. Allows incremental registration.
	 * 
	 * @param url
	 *            the <code>ServiceURL</code> of the service.
	 * @param attributes
	 *            the attributes to be added.
	 * @throws ServiceLocationException
	 *             whenever called.
	 */
	void addAttributes(ServiceURL url, Dictionary attributes)
			throws ServiceLocationException;

	/**
	 * <b>Not yet implemented.</b> Remove attributes to an already registered
	 * service. Allows incremental registration.
	 * 
	 * @param url
	 *            the <code>ServiceURL</code> of the service.
	 * @param attributeIds
	 *            the attributes to be removed.
	 * @throws ServiceLocationException
	 *             whenever called.
	 */
	void deleteAttributes(ServiceURL url, Dictionary attributeIds)
			throws ServiceLocationException;

	/**
	 * Get the IP address of this machine that is configured as primary jSLP
	 * address. Can be used to register Services that are located on this host.
	 * 
	 * @return the local InetAddress.
	 */
	InetAddress getMyIP();

}
