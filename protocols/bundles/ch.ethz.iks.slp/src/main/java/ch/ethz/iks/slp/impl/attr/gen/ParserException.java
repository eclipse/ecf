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
