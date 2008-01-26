package org.eclipse.ecf.internal.discovery.ui;

import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.ui.views.IServiceAccessHandler;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;

public class NullServiceAccessHandler implements IServiceAccessHandler {

	public NullServiceAccessHandler() {
		// Nothing todo
	}

	public IContributionItem[] getContributionsForService(IServiceInfo serviceInfo) {
		final IAction action = new Action() {
			public void run() {
				System.out.println("run!!"); //$NON-NLS-1$
			}
		};
		action.setText("Boo!"); //$NON-NLS-1$
		return new IContributionItem[] {new ActionContributionItem(action)};
	}

}
