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


/*
 * Interface contract for dynamic component registration/
 * deregistration with a Eclipse Project and it's associated
 * group.
 * 
 * @author slewis
 */
public interface EclipseProjectComponent {

	public static final String INVOKE_METHOD_NAME = "invoke";

	/**
	 * Method called when this component is instantiated and
	 * registered with the associated EclipseProject.  Component implementers
	 * may override this method in order to initialize, setup ui for 
	 * this component, or perform some other component startup functions
	 * 
	 * @param obj
	 * @param requestor
	 * @throws Exception
	 */
	public void register(EclipseProject obj, User requestor)
		throws Exception;

	/**
	 * This method is invoked when a message is sent to the given 
	 * component
	 * 
	 * @param meth
	 * @param args
	 * @return
	 */
	public Object invoke(String meth, Object[] args);

	/**
	 * Method called when this component is removed from the 
	 * associated EclipseProject.  Component implementers
	 * may override this method in order to cleanup during component shutdown
	 * 
	 * @param obj
	 * @param requestor
	 * @throws Exception
	 */
	public void deregister(EclipseProject obj);

}
