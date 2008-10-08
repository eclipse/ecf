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

import java.util.List;
import java.util.Locale;

/**
 * Locator implements the UA properties of SLP. Services can be discovered by
 * type or by URL, attributes of discovered services can be retrieved and
 * service types can be listed.
 * 
 * @author Jan S. Rellermeyer, IKS, ETH Zurich
 * @since 0.1
 */
public interface Locator {
	/**
	 * Returns the locale of this Locator instance.
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
	 * Find all services types that are currently
	 * registered in the network.
	 * 
	 * @param namingAuthority
	 *            the naming authority for the service type. If omitted,
	 *            ALL Service Types are returned, regardless of Naming Authority.
	 *            With the empty <code>String</code> (""), <code>IANA</code> will be assumed.
	 * @param scopes
	 *            a <code>List</code> of scopes in that service types are to
	 *            be discovered.
	 * @return a ServiceLocationEnumeration over the discovered ServiceTypes.
	 * @throws ServiceLocationException
	 *             whenever called.
	 */
	ServiceLocationEnumeration findServiceTypes(String namingAuthority,
			List scopes) throws ServiceLocationException;

	/**
	 * Find all services that match a certain service type.
	 * 
	 * @param type
	 *            the ServiceType.
	 * @param scopes
	 *            A <code>List</code> of scope <code>Strings</code>, RFC
	 *            2614 uses <code>Vector</code> here but jSLP prefers the
	 *            Collection Framework.
	 * @param searchFilter
	 *            an RFC 1960 compliant <code>String</code> of a LDAP filter.
	 *            RFC 2614 proposes the newer RFC 2254 style filters that adds
	 *            support for extensible matches.
	 * @return a ServiceLocationEnumeration over the <code>ServiceURLs</code>
	 *         of the found services.
	 * @throws ServiceLocationException
	 *             in case of an exception in the underlying framework.
	 * @throws InvalidSyntaxException 
	 */
	ServiceLocationEnumeration findServices(ServiceType type, List scopes,
			String searchFilter) throws ServiceLocationException, IllegalArgumentException;

	/**
	 * Find all services that match a ServiceURL.
	 * 
	 * @param url
	 *            the ServiceURL.
	 * @param scopes
	 *            A <code>List</code> of scopes <code>Strings</code>, RFC
	 *            2614 uses <code>Vector</code> here but jSLP prefers the
	 *            Collection Framework.
	 * @param attributeIds
	 *            A List of attribute-value-pairs like
	 * 
	 * <pre>
	 * (key = value)
	 * </pre>
	 * 
	 * that must match. If null, no attribute constraints are applied.
	 * @return a ServiceLocationEnumeration over the <code>ServiceURLs</code>
	 *         of the found services.
	 * @throws ServiceLocationException
	 *             in case of an exception in the underlying framework.
	 */
	ServiceLocationEnumeration findAttributes(ServiceURL url, List scopes,
			List attributeIds) throws ServiceLocationException;

	/**
	 * Find all services that match a ServiceType.
	 * 
	 * @param type
	 *            the ServiceType.
	 * @param scopes
	 *            A <code>List</code> of scope <code>Strings</code>, RFC
	 *            2614 uses <code>Vector</code> here but jSLP prefers the
	 *            Collection Framework.
	 * @param attributeIds
	 *            A List of attribute-value-pairs like
	 * 
	 * <pre>
	 * (key = value)
	 * </pre>
	 * 
	 * that must match. If null, no attribute constraints are applied.
	 * @return a ServiceLocationEnumeration over the ServiceURLs of the found
	 *         services.
	 * @throws ServiceLocationException
	 *             in case of an exception in the underlying framework.
	 */
	ServiceLocationEnumeration findAttributes(ServiceType type, List scopes,
			List attributeIds) throws ServiceLocationException;
}
