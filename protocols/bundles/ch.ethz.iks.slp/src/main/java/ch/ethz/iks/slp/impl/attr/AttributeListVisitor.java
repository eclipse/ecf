/****************************************************************************
 * Copyright (c) 2008 Versant Corp.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Markus Kuppe (mkuppe <at> versant <dot> com) - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package ch.ethz.iks.slp.impl.attr;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.iks.slp.impl.attr.gen.Displayer;
import ch.ethz.iks.slp.impl.attr.gen.Parser;
import ch.ethz.iks.slp.impl.attr.gen.Parser.attribute;

public class AttributeListVisitor extends Displayer {
	private List result = new ArrayList();

	/*
	 * (non-Javadoc)
	 * @see ch.ethz.iks.slp.impl.attr.Displayer#visit_attribute(ch.ethz.iks.slp.impl.attr.Parser.attribute)
	 */
	public Object visit_attribute(attribute rule) {
		result.add(rule.spelling);
		return null;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.slp.impl.attr.gen.Displayer#visit_StringValue(ch.ethz.iks.slp.impl.attr.gen.Parser.StringValue)
	 */
	public Object visit_StringValue(Parser.StringValue value) {
		// overwrite to not print anything to System.out
		return null;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.slp.impl.attr.gen.Displayer#visit_NumericValue(ch.ethz.iks.slp.impl.attr.gen.Parser.NumericValue)
	 */
	public Object visit_NumericValue(Parser.NumericValue value) {
		// overwrite to not print anything to System.out
		return null;
	}

	public List getAttributes() {
		return result;
	}
}
