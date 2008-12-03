/* -----------------------------------------------------------------------------
 * Visitor.java
 * -----------------------------------------------------------------------------
 *
 * Producer : com.parse2.aparse.Parser 0.5
 * Produced : Tue Dec 02 14:25:41 CET 2008
 *
 * -----------------------------------------------------------------------------
 */

package ch.ethz.iks.slp.impl.attr.gen;

public interface Visitor
{
  public void visit(Rule rule);

  public Object visit_attr_list(Parser.attr_list rule);
  public Object visit_attribute(Parser.attribute rule);
  public Object visit_attr_val_list(Parser.attr_val_list rule);
  public Object visit_attr_tag(Parser.attr_tag rule);
  public Object visit_attr_val(Parser.attr_val rule);
  public Object visit_intval(Parser.intval rule);
  public Object visit_strval(Parser.strval rule);
  public Object visit_boolval(Parser.boolval rule);
  public Object visit_opaque(Parser.opaque rule);
  public Object visit_safe_val(Parser.safe_val rule);
  public Object visit_safe_tag(Parser.safe_tag rule);
  public Object visit_escape_val(Parser.escape_val rule);
  public Object visit_DIGIT(Parser.DIGIT rule);
  public Object visit_HEXDIG(Parser.HEXDIG rule);
  public Object visit_StringValue(Parser.StringValue value);
  public Object visit_NumericValue(Parser.NumericValue value);
}

/* -----------------------------------------------------------------------------
 * eof
 * -----------------------------------------------------------------------------
 */
