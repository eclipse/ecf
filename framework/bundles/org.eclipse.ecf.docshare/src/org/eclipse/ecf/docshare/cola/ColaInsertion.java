/****************************************************************************
 * Copyright (c) 2008 Mustafa K. Isik and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mustafa K. Isik - conflict resolution via operational transformations
 *****************************************************************************/

package org.eclipse.ecf.docshare.cola;

public class ColaInsertion implements TransformationStrategy {

	public ColaUpdateMessage getForOwner(ColaUpdateMessage toBeTransformed, ColaUpdateMessage alreadyApplied) {
		// i.e. this strategy belongs to an operation/msg coming from a
		// participant-->lesser prio
		// remote is to be properly transformed
		if (toBeTransformed.getOffset() > alreadyApplied.getOffset() && toBeTransformed.getOffset() < (alreadyApplied.getOffset() + alreadyApplied.getText().length())) {
			// the modification
		}
		return null;
	}

	public ColaUpdateMessage getForParticipant(ColaUpdateMessage toBeTransformed, ColaUpdateMessage alreadyApplied) {
		// TODO Auto-generated method stub
		return null;
	}

}
