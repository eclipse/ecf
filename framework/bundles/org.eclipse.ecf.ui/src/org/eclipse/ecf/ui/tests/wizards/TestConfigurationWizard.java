package org.eclipse.ecf.ui.tests.wizards;

import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.ui.Activator;
import org.eclipse.ecf.internal.ui.UIDebugOptions;
import org.eclipse.ecf.ui.IConfigurationWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;

public class TestConfigurationWizard extends Wizard implements
		IConfigurationWizard {

	ContainerTypeDescription typeDescription;
	
	IWorkbench workbench;
	
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return false;
	}

	public void init(IWorkbench workbench,
			ContainerTypeDescription containerDescription) {
		// TODO Auto-generated method stub
		Trace.entering(Activator.getDefault(), UIDebugOptions.METHODS_ENTERING, this.getClass(), "init", new Object [] { workbench, containerDescription });
		System.out.println("TestConfigurationWizard.init("+workbench+","+containerDescription+")");
	}

	public IContainer getConfigurationResult() {
		// TODO Auto-generated method stub
		return null;
	}

}
