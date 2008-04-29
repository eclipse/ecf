package org.eclipse.ecf.docshare.cola;

public class ColaInsertion implements TransformationStrategy {

	public ColaUpdateMessage getForOwner(ColaUpdateMessage toBeTransformed,
			ColaUpdateMessage alreadyApplied) {
		// i.e. this strategy belongs to an operation/msg coming from a
		// participant-->lesser prio
		// remote is to be properly transformed
		if (toBeTransformed.getOffset() > alreadyApplied.getOffset()
				&& toBeTransformed.getOffset() < (alreadyApplied.getOffset() + alreadyApplied.getText()
						.length())) {
			// the modification
		}
		return null;
	}

	public ColaUpdateMessage getForParticipant(ColaUpdateMessage toBeTransformed,
			ColaUpdateMessage alreadyApplied) {
		// TODO Auto-generated method stub
		return null;
	}

}
