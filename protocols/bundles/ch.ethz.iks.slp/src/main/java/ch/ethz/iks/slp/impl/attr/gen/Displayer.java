/* -----------------------------------------------------------------------------
 * Displayer.java
 * -----------------------------------------------------------------------------
 *
 * Producer : com.parse2.aparse.Parser 0.5
 * Produced : Tue Dec 02 14:25:41 CET 2008
 *
 * -----------------------------------------------------------------------------
 */

package ch.ethz.iks.slp.impl.attr.gen;

import java.util.ArrayList;
import java.util.Iterator;

public class Displayer implements Visitor
{
  public void visit(Rule rule)
  {
    rule.visit(this);
  }

  public Object visit_attr_list(Parser.attr_list rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit_attribute(Parser.attribute rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit_attr_val_list(Parser.attr_val_list rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit_attr_tag(Parser.attr_tag rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit_attr_val(Parser.attr_val rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit_intval(Parser.intval rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit_strval(Parser.strval rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit_boolval(Parser.boolval rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit_opaque(Parser.opaque rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit_safe_val(Parser.safe_val rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit_safe_tag(Parser.safe_tag rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit_escape_val(Parser.escape_val rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit_DIGIT(Parser.DIGIT rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit_HEXDIG(Parser.HEXDIG rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit_StringValue(Parser.StringValue value)
  {
    System.out.print(value.spelling);
    return null;
  }

  public Object visit_NumericValue(Parser.NumericValue value)
  {
    System.out.print(value.spelling);
    return null;
  }

  private Object visitRules(ArrayList rules)
  {
    for (Iterator i = rules.iterator(); i.hasNext();)
      ((Rule)i.next()).visit(this);
    return null;
  }
}

/* -----------------------------------------------------------------------------
 * eof
 * -----------------------------------------------------------------------------
 */
