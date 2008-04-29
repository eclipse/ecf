package org.eclipse.ecf.docshare.cola;

public interface TransformationStrategy {

	ColaUpdateMessage getForOwner(ColaUpdateMessage toBeTransformed,
			ColaUpdateMessage alreadyApplied);

	ColaUpdateMessage getForParticipant(ColaUpdateMessage toBeTransformed,
			ColaUpdateMessage alreadyApplied);
}
