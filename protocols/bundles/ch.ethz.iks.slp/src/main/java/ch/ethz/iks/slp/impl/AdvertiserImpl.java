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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.List;
import java.util.Locale;
import ch.ethz.iks.slp.Advertiser;
import ch.ethz.iks.slp.ServiceLocationException;
import ch.ethz.iks.slp.ServiceURL;

/**
 * Implementation of the Advertiser that provides SLP SA functionality. If the
 * configuration does not have to support SA functionalities, this class does
 * not have to be included in the distribution.
 * 
 * @see ch.ethz.iks.slp.Advertiser
 * @author Jan S. Rellermeyer, IKS, ETH Zurich
 * @since 0.1
 */
public final class AdvertiserImpl implements Advertiser {

	/**
	 * the locale of this instance. Will be used for all messages created by
	 * this Advertiser instance.
	 */
	private Locale locale;

	/**
	 * Constructor for AdvertiserImpl.
	 */
	public AdvertiserImpl() {
		locale = SLPCore.DEFAULT_LOCALE;
	}
	
	/**
	 * Constructor for AdvertiserImpl.
	 * 
	 * @param theLocale
	 *            Locale.
	 */
	public AdvertiserImpl(final Locale locale) {
		this.locale = locale;
	}	

	/**
	 * Get the locale of this instance.
	 * 
	 * @return Locale.
	 * @see Advertiser#getLocale()
	 */
	public Locale getLocale() {
		return locale;
	}
	
	/**
	 * Set the locale of this instance.
	 * 
	 * @param locale
	 *            the Locale.
	 * @see Advertiser#setLocale()
	 */
	public void setLocale(final Locale locale) {
		this.locale = locale;
	}

	/**
	 * register a new service with the framework.
	 * 
	 * @param url
	 *            the ServiceURL of the service.
	 * @param attributes
	 *            a Dictionary of attributes.
	 * @throws ServiceLocationException
	 *             if the registration has failed for any reason.
	 * @see Advertiser#register(ServiceURL, Dictionary)
	 */
	public void register(final ServiceURL url, final Dictionary attributes)
			throws ServiceLocationException {
		register(url, null, attributes);
	}

	/**
	 * register a new service with the framework using scopes.
	 * 
	 * @param url
	 *            the ServiceURL of the service.
	 * @param scopes
	 *            a List of scopes.
	 * @param attributes
	 *            a Dictionary of attributes.
	 * @throws ServiceLocationException
	 *             if the registration has failed for any reason.
	 * @see Advertiser#register(ServiceURL, List, Dictionary)
	 */
	public void register(final ServiceURL url, final List scopes,
			final Dictionary attributes) throws ServiceLocationException {
		ServiceRegistration reg = new ServiceRegistration(url, url
				.getServiceType(), scopes, SLPUtils.dictToAttrList(attributes),
				locale);
		try {
			reg.address = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			reg.address = SLPCore.getMyIP();
		}
		reg.port = SLPCore.SLP_PORT;
		ServiceAcknowledgement ack = (ServiceAcknowledgement) SLPCore
				.sendMessage(reg, true);
		if (ack.errorCode != 0) {
			throw new ServiceLocationException((short) ack.errorCode,
					"Registration failed");
		}
	}

	/**
	 * deregister a service.
	 * 
	 * @param url
	 *            the ServiceURL of the service.
	 * @throws ServiceLocationException
	 *             if the deregistration has failed for any reason.
	 * @see Advertiser#deregister(ServiceURL)
	 */
	public void deregister(final ServiceURL url)
			throws ServiceLocationException {
		deregister(url, null);
	}

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

	public void deregister(final ServiceURL url, final List scopes)
			throws ServiceLocationException {
		ServiceDeregistration dereg = new ServiceDeregistration(url, scopes,
				null, locale);
		try {
			dereg.address = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			dereg.address = SLPCore.getMyIP();
		}
		dereg.port = SLPCore.SLP_PORT;
		ServiceAcknowledgement ack = (ServiceAcknowledgement) SLPCore
				.sendMessage(dereg, true);
		if (ack.errorCode != 0) {
			throw new ServiceLocationException((short) ack.errorCode,
					"Deregistration failed");
		}
	}

	/**
	 * currently not supported.
	 * 
	 * @see Advertiser#addAttributes(ServiceURL, Dictionary)
	 * @param url
	 *            the serviceURL
	 * @param attributes
	 *            the attributes to add.
	 * @throws ServiceLocationException
	 *             always.
	 */
	public void addAttributes(final ServiceURL url, final Dictionary attributes)
			throws ServiceLocationException {
		throw new ServiceLocationException(
				ServiceLocationException.NOT_IMPLEMENTED,
				"incremental registration not supported");
	}

	/**
	 * currently not supported.
	 * 
	 * @see Advertiser#deleteAttributes(ServiceURL, Dictionary)
	 * @param url
	 *            the serviceURL.
	 * @param attributes
	 *            the attributes to delete.
	 * @throws ServiceLocationException
	 *             always.
	 */
	public void deleteAttributes(final ServiceURL url,
			final Dictionary attributes) throws ServiceLocationException {
		throw new ServiceLocationException(
				ServiceLocationException.NOT_IMPLEMENTED,
				"incremental registration not supported");
	}

	/**
	 * Get the IP address of this machine that is configured as primary jSLP
	 * address. Can be used to register Services that are located on this host.
	 * 
	 * @return the local InetAddress.
	 */
	public InetAddress getMyIP() {
		try {
			return SLPCore.getMyIP();
		} catch (Exception e) {
			return null;
		}
	}
}
