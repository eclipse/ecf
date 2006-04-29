package org.eclipse.ecf.example.collab.start;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.start.IECFStart;

public class CollabStart implements IECFStart {
	
	Discovery discovery = null;
	
	public IStatus startup(IProgressMonitor monitor) {
		System.out.println("CollabSTart.startup");
		try {
			//discovery = new Discovery();
		} catch (Exception e) {}
		return new Status(IStatus.OK,"org.eclipse.ecf",100,"",null);
	}

}
