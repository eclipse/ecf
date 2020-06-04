/****************************************************************************
 * Copyright (c) 2005, 2010 Jan S. Rellermeyer, Systems Group,
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Jan S. Rellermeyer - initial API and implementation
 *    Markus Alexander Kuppe - enhancements and bug fixes
 * 
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package ch.ethz.iks.slp.impl;

import ch.ethz.iks.slp.ServiceLocationException;

/**
 * the SLPDeaemon interface. Factored out to make the daemon part optional as
 * part of the jSLP modularity.
 * 
 * @author Jan S. Rellermeyer, ETH Zurich.
 */
public interface SLPDaemon {

	/**
	 * called, when a new DA has been discovered.
	 * 
	 * @param advert
	 *            the <code>DAAdvertisement</code> received from the new DA.
	 */
	void newDaDiscovered(DAAdvertisement advert);

	/**
	 * handle a message dispatched by SLPCore.
	 * 
	 * @param msg
	 *            the message.
	 * @return the reply message or <code>null</code>.
	 * @throws ServiceLocationException
	 *             if something goes wrong.
	 */
	ReplyMessage handleMessage(final SLPMessage msg)
			throws ServiceLocationException;

}
