package org.eclipse.ecf.example.clients;

import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.sharedobject.AbstractSharedObject;

public class TrivialSharedObject extends AbstractSharedObject {

	public TrivialSharedObject() {
		super();
		System.out.println("TrivialSharedObject is here!");
	}

	protected void initialize() throws SharedObjectInitException {
		super.initialize();
		System.out.println("TrivialSharedObject with id "+getID());
	}
	
}
