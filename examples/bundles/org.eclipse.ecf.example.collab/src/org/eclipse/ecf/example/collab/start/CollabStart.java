package org.eclipse.ecf.example.collab.start;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.start.IECFStart;

public class CollabStart implements IECFStart {
	public IStatus start(IProgressMonitor monitor) {
		System.out.println("COLLABSTART!!!!");
		try {
			Thread.sleep(20000);
		} catch (Exception e) {}
		return null;
	}
	
	public void stop() {
		// TODO Auto-generated method stub
	}
}
