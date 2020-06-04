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
 * Abstract base class for all request messages.
 *
 * @author Jan S. Rellermeyer, ETH Z�rich
 * @since 0.1
 */
abstract class RequestMessage extends SLPMessage {
    /**
     * the list of previous responders. If a peer receives a request message and
     * is already in the previous responder list, it will silently drop the
     * message.
     */
    List prevRespList;

    /**
     * a list of scopes that will be included.
     */
    List scopeList;
    
}
