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
 * Rule.java
 * -----------------------------------------------------------------------------
 *
 * Producer : com.parse2.aparse.Parser 0.5
 * Produced : Tue Dec 02 14:25:41 CET 2008
 *
 * -----------------------------------------------------------------------------
 */

package ch.ethz.iks.slp.impl.attr.gen;

import java.util.ArrayList;

public abstract class Rule
{
  public final String spelling;
  public final ArrayList rules;

  protected Rule(String spelling, ArrayList rules)
  {
    this.spelling = spelling;
    this.rules = rules;
  }

  public Rule(Rule rule)
  {
    this(rule.spelling, rule.rules);
  }

  public String toString()
  {
    return spelling;
  }

  public boolean equals(Object object)
  {
    return object instanceof Rule && spelling.equals(((Rule)object).spelling);
  }

  public int hashCode()
  {
    return spelling.hashCode();
  }

  public int compareTo(Rule rule)
  {
    return spelling.compareTo(rule.spelling);
  }

  public abstract Object visit(Visitor visitor);
}

/* -----------------------------------------------------------------------------
 * eof
 * -----------------------------------------------------------------------------
 */
