/****************************************************************************
 * Copyright (c) 2005, 2010 Jan S. Rellermeyer, Systems Group,
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Markus Alexander Kuppe - initial API and implementation
 * 
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
/* -----------------------------------------------------------------------------
 * ParserException.java
 * -----------------------------------------------------------------------------
 *
 * Producer : com.parse2.aparse.Parser 0.5
 * Produced : Tue Dec 02 14:25:41 CET 2008
 *
 * -----------------------------------------------------------------------------
 */

package ch.ethz.iks.slp.impl.attr.gen;

/**
 * Changed, do not overwrite added!!!
 */
public class ParserException extends Exception {
	private static final long serialVersionUID = -3319122582148082535L;
	private Rule rule;

	public ParserException(String message) {
		super(message);
	}

	public ParserException(String string, Rule aRule) {
		super(string);
		rule = aRule;
	}
	
	/**
	 * @return the rule
	 */
	public Rule getRule() {
		return rule;
	}
}
