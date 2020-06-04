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

import java.util.List;

/**
 * abstract base class for all ReplyMessages.
 * 
 * @author Jan S. Rellermeyer, ETH Z�rich
 * @since 0.1
 */
abstract class ReplyMessage extends SLPMessage {

	/**
	 * the error code that is returned.
	 */
	int errorCode;

	/**
	 * get the results.
	 * 
	 * @return the List of results.
	 */
	abstract List getResult();

}
