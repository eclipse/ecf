/*******************************************************************************
 * Copyright (c) 2015 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Scott Lewis - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.remoteserviceadmin.ui.rsa;

import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin;
import org.eclipse.ecf.remoteserviceadmin.ui.rsa.model.AbstractRegistrationNode;
import org.eclipse.ecf.remoteserviceadmin.ui.rsa.model.ExportRegistrationNode;
import org.eclipse.ecf.remoteserviceadmin.ui.rsa.model.ImportRegistrationNode;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ITreeSelection;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminEvent;

/**
 * @since 3.3
 */
public class RemoteServiceAdminView extends AbstractRemoteServiceAdminView {

	public static final String ID_VIEW = "org.eclipse.ecf.remoteserviceadmin.ui.views.RSAView"; //$NON-NLS-1$

	private Action closeExportAction;
	private Action closeImportAction;

	public RemoteServiceAdminView() {
	}

	protected void fillContextMenu(IMenuManager manager) {
		ITreeSelection selection = (ITreeSelection) viewer.getSelection();
		if (selection != null) {
			Object e = selection.getFirstElement();
			if (e instanceof ImportRegistrationNode) {
				manager.add(closeImportAction);
			} else if (e instanceof ExportRegistrationNode) {
				manager.add(closeExportAction);
			}
		}
	}

	protected void makeActions() {
		RemoteServiceAdmin rsa = getRSA();
	
		closeExportAction = createCloseAction();
		closeExportAction.setText("Unexport Service");
		closeExportAction.setEnabled(rsa != null);
	
		closeImportAction = createCloseAction();
		closeImportAction.setText("Unimport Service");
		closeImportAction.setEnabled(rsa != null);
	
	}

	protected Action createCloseAction() {
		return new Action() {
			public void run() {
				AbstractRegistrationNode n = getSelectedRegistrationNode();
				if (n != null) 
					n.close();
			}
		};
	}


	public void handleRSAEvent(final RemoteServiceAdminEvent event) {
		if (viewer == null)
			return;
		viewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				RemoteServiceAdmin rsa = getRSA();
				if (rsa != null) {
					switch (event.getType()) {
					case RemoteServiceAdminEvent.EXPORT_REGISTRATION:
					case RemoteServiceAdminEvent.EXPORT_UNREGISTRATION:
					case RemoteServiceAdminEvent.EXPORT_ERROR:
					case RemoteServiceAdminEvent.EXPORT_UPDATE:
					case RemoteServiceAdminEvent.EXPORT_WARNING:
						updateModel(1);
						break;
					case RemoteServiceAdminEvent.IMPORT_REGISTRATION:
					case RemoteServiceAdminEvent.IMPORT_UNREGISTRATION:
					case RemoteServiceAdminEvent.IMPORT_ERROR:
					case RemoteServiceAdminEvent.IMPORT_UPDATE:
					case RemoteServiceAdminEvent.IMPORT_WARNING:
						updateModel(2);
						break;
					}
				}
			}
		});
	}

}
