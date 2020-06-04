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

package ch.ethz.iks.slp.impl.filter;

import java.util.Dictionary;

/**
 * a generic LDAP filter.
 * @author Jan S. Rellermeyer, ETH Zurich
 *
 */
public interface Filter {
    /**
     * try to match a <code>Dictionary</code> of attributes.
     * @param values a <code>Dictionary</code> of attributes.
     * @return true if the filter evaluated to true;
     */
    boolean match(Dictionary values);

    /**
     * get a String representation of the filter.
     * @return the String representation.
     */
    String toString();
}
