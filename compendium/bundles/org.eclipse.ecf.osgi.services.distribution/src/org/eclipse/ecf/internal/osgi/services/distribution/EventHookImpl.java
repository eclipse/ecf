/*******************************************************************************
* Copyright (c) 2009 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.distribution;

import java.util.Collection;

import org.eclipse.ecf.core.util.Trace;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.hooks.service.EventHook;

public class EventHookImpl implements EventHook {

	public void event(ServiceEvent event, Collection contexts) {
		Trace.entering(Activator.PLUGIN_ID,DebugOptions.METHODS_ENTERING,this.getClass(),"event");
		handleEvent(event,contexts);
		Trace.exiting(Activator.PLUGIN_ID, DebugOptions.METHODS_EXITING, this.getClass(), "event");
	}

	private void handleEvent(ServiceEvent event, Collection contexts) {
		// TODO Auto-generated method stub
		
	}

}
