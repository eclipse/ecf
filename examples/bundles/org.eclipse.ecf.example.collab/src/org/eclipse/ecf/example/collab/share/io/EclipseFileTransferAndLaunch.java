/****************************************************************************
* Copyright (c) 2004 Composent, Inc. and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Composent, Inc. - initial API and implementation
*****************************************************************************/

package org.eclipse.ecf.example.collab.share.io;

import org.eclipse.ecf.example.collab.ClientPlugin;
import org.eclipse.ecf.example.collab.ui.MessageLoader;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;


public class EclipseFileTransferAndLaunch
	extends EclipseFileTransfer {
		
    public EclipseFileTransferAndLaunch() {
        
    }
	public void sendDone(FileTransferSharedObject obj, Exception e) {
		if (senderUI != null) {
			senderUI.sendDone(transferParams.getRemoteFile(), e);
		} else {
			System.out.println("Sending done for: "+transferParams.getRemoteFile());
		}
		// Now launch file locally
		if (e == null) launchFile(transferParams.getRemoteFile().getAbsolutePath());
	}
	protected void launchFile(String fileName) {
		try {
		    Program.launch(fileName);
		} catch (final IllegalArgumentException e1) {
		    ClientPlugin.log("Exception launching local file "+localFile,e1);
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(null,
					        MessageLoader.getString("Program Launch Failed"),
					        MessageLoader.getString("Program launch failed for file '"+localFile.getAbsolutePath()+"'\nException: "+e1.getMessage()));
				}
			});
		}
	}
	public void receiveDone(FileTransferSharedObject obj, Exception e) {
		// Need GUI progress indicator here
		if (receiverUI != null) {
			receiverUI.receiveDone(getHomeContainerID(),localFile,e);
		} else {
			System.out.println("Receiving done for: "+localFile);
		}
		// Now...we launch the file
		if (e == null) {
		    launchFile(localFile.getAbsolutePath());
		}
	}

}