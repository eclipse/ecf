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
package ch.ethz.iks.slp;

import java.util.Enumeration;

/**
 * An enumeration over results of request messages as defined in RFC 2614.
 * 
 * @author Jan S. Rellermeyer, Systems Group, ETH Zurich
 * @since 0.1
 */
public interface ServiceLocationEnumeration extends Enumeration {

    /**
     * get the next result of a request.
     *
     * @return the next <code>Object</code>
     * @throws ServiceLocationException
     *             if there is no more result.
     */
    Object next() throws ServiceLocationException;
}
