/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.generic;

import org.eclipse.ecf.core.ISharedObjectContainerListener;
import org.eclipse.ecf.core.events.IContainerEvent;


class ContainerListener {
    ISharedObjectContainerListener listener;
    String filter;
    
    ContainerListener(ISharedObjectContainerListener l, String filter) {
        this.listener = l;
        this.filter = filter;
    }
    protected IContainerEvent applyFilter(IContainerEvent evt) {
        return evt;
    }
    protected void handleEvent(IContainerEvent evt) {
        if (listener != null) {
            IContainerEvent event = applyFilter(evt);
            listener.handleEvent(event);
        }
    }
    protected boolean isListener(ISharedObjectContainerListener l) {
        if (listener == null) return false;
        if (l == null) return false;
        if (listener.equals(l)) return true;
        return false;
    }
}