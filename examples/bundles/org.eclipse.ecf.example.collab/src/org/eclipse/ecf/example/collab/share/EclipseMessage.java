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

import java.util.HashMap;
import java.util.Map;
import org.eclipse.ecf.core.ISharedObjectConfig;
import org.eclipse.ecf.core.SharedObjectDescription;
import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.example.collab.Trace;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

public class EclipseMessage extends GenericSharedObject {
    public static final Trace trace = Trace
            .create("eclipsemessagesharedobject");
    String message;
    String sender;

    public EclipseMessage() {
        sender = "<unknown>";
        message = " says hello";
    }

    public EclipseMessage(String message, String sender) {
        this.message = message;
        this.sender = sender;
    }

    protected void trace(String msg) {
        if (Trace.ON && trace != null) {
            trace.msg(msg);
        }
    }

    protected void traceDump(String msg, Throwable e) {
        if (Trace.ON && trace != null) {
            trace.dumpStack(e, msg);
        }
    }

    public void init(ISharedObjectConfig config)
            throws SharedObjectInitException {
        super.init(config);
        Map aMap = config.getProperties();
        Object[] args = (Object[]) aMap.get("args");
        if (args != null && args.length == 2) {
            this.message = (String) args[0];
            this.sender = (String) args[1];
        }
    }

    public void activated(ID[] others) {
        // Note: be sure to call super.activated first so
        // replication gets done
        super.activated(others);
        showMessage(message, sender);
    }

    protected SharedObjectDescription getReplicaDescription(ID remoteID) {
        Object[] remoteArgs = { message, sender };
        HashMap map = new HashMap();
        map.put("args", remoteArgs);
        return new SharedObjectDescription(getHomeContainerID(), getClass()
                .getName(), map, replicateID++);
    }

    protected void showMessage(final String message, final String sender) {
        final String msg = sender + " says '" + message + "'";
        try {
            if (!getContext().isGroupManager()) {
                Display.getDefault().syncExec(new Runnable() {
                    public void run() {
                        Display.getDefault().beep();
                        MessageDialog.openInformation(null, "Message from "
                                + sender, msg);
                    }
                });
            }
        } catch (Exception e) {
            traceDump("Exception showing message dialog ", e);
        }
        destroySelf();
    }
}
