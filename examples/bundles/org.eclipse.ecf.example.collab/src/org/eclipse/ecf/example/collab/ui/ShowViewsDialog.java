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

package org.eclipse.ecf.example.collab.ui;

import org.eclipse.ecf.example.collab.share.User;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.ShowViewDialog;
import org.eclipse.ui.internal.registry.IViewDescriptor;

public class ShowViewsDialog extends ShowViewDialog {
    
    LineChatHandler lch;
    
    public ShowViewsDialog(Shell shell, LineChatHandler lch) {
        super(shell,WorkbenchPlugin.getDefault().getViewRegistry());
        this.lch = lch;
    }
    
    public void showViews(User touser) {
		IViewDescriptor[] descs = getSelection();
		if (descs==null) return;
		for (int i = 0; i < descs.length; ++i) {
		    lch.sendShowView(touser,descs[i].getID());
		}
    }
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(MessageLoader
				.getString("LineChatClientView.contextmenu.sendShowViewRequest.dialog.title")); 
		//WorkbenchHelp.setHelp(shell, IHelpContextIds.SHOW_VIEW_DIALOG);
	}
}
