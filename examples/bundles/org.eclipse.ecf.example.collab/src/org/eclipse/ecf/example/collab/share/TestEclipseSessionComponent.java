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

package org.eclipse.ecf.example.collab.share;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

public class TestEclipseSessionComponent implements EclipseProjectComponent {
    EclipseProject repobj = null;
    User requestor = null;

    public TestEclipseSessionComponent() {
        super();
    }

    public Object invoke(String meth, Object[] args) {
        String argStr = "";
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                argStr = argStr + "\t\t" + args[i].toString() + "\n";
            }
        }
        showMsg("EclipseProjectComponent " + this.getClass().getName()
                + " invoke", "Method: '" + meth + "' \n\tArgs:\n" + argStr);
        return null;
    }

    public void register(EclipseProject obj, final User r)
            throws Exception {
        this.repobj = obj;
        this.requestor = r;
        showMsg("Registration request from " + requestor.getNickname()
                + ".  ID: " + requestor.getUserID(),
                "EclipseProjectComponent '" + this.getClass().getName()
                        + "' registered");
    }

    public void showMessage(String msg) {
        showMsg("EclipseProjectComponent Message", msg);
    }

    protected void showMsg(final String title, final String msg) {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                MessageDialog.openInformation(null, title, msg);
            }
        });
    }

    public void deregister(EclipseProject obj) {
        showMsg("Deregistration request from " + requestor.getNickname()
                + ".  ID: " + requestor.getUserID(),
                "EclipseProjectComponent '" + this.getClass().getName()
                        + "' deregistered");
        repobj = null;
        requestor = null;
    }
}
