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
package ch.ethz.iks.slp;

import java.util.Locale;
import ch.ethz.iks.slp.impl.SLPCore;
import ch.ethz.iks.slp.impl.StandalonePlatformAbstraction;

/**
 * The central manager for SLP interaction. Application can get a Locator for UA
 * functionality and a Advertiser for SA functionality.
 * 
 * @author Jan S. Rellermeyer, ETH Zurich
 * @since 0.1
 */
public final class ServiceLocationManager extends SLPCore {

	/**
	 * hidden default constructor.
	 */
	private ServiceLocationManager() {
	}

	public static void init() {
		if(SLPCore.platform == null) {
			SLPCore.platform = new StandalonePlatformAbstraction();
			SLPCore.init();
		}
	}
	
	/**
	 * get the refresh interval, that is the maximum over all DA's minimum
	 * update intervals.
	 * 
	 * @return the refresh interval.
	 * @throws ServiceLocationException
	 *             in case of an exception in the underlying framework.
	 */
	public static int getRefreshInterval() throws ServiceLocationException {
		return -1;
	}

	/**
	 * get a Locator to have access to the UA functionalities.
	 * 
	 * @param locale
	 *            the <code>Locale</code> for all requests.
	 * @return a <code>Locator</code> instance.
	 * @throws ServiceLocationException
	 *             in case of an exception in the underlying framework.
	 */
	public static Locator getLocator(final Locale locale)
			throws ServiceLocationException {
		init();
		if (locator != null) {
			try {
				return (Locator) locator.newInstance(new Object[] { locale });
			} catch (Exception e) {
				throw new ServiceLocationException(
						ServiceLocationException.INTERNAL_SYSTEM_ERROR, e
								.getMessage());
			}
		} else {
			throw new ServiceLocationException(
					ServiceLocationException.NOT_IMPLEMENTED,
					"The current configuration does not support UA functionalities.");
		}
	}

	/**
	 * get a Advertiser to have access to the SA functionalities.
	 * 
	 * @param locale
	 *            the <code>Locale</code> for all messages.
	 * @return an <code>Advertiser</code> instance.
	 * @throws ServiceLocationException
	 *             in case of an exception in the underlying framework.
	 */
	public static Advertiser getAdvertiser(final Locale locale)
			throws ServiceLocationException {
		init();
		SLPCore.initMulticastSocket();
		if (advertiser != null) {
			try {
				return (Advertiser) advertiser
						.newInstance(new Object[] { locale });
			} catch (Exception e) {
				throw new ServiceLocationException(
						ServiceLocationException.INTERNAL_SYSTEM_ERROR, e
								.getMessage());
			}
		} else {
			throw new ServiceLocationException(
					ServiceLocationException.NOT_IMPLEMENTED,
					"The current configuration does not support SA functionalities.");
		}
	}
}
