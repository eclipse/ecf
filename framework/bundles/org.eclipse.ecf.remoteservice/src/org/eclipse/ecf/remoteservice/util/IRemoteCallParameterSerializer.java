/*******************************************************************************
* Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.remoteservice.util;

import java.io.NotSerializableException;
import org.eclipse.ecf.remoteservice.IRemoteCallParameter;

/**
 * @since 3.3
 */
public interface IRemoteCallParameterSerializer {

	public String serializeRemoteCallParameter(IRemoteCallParameter defaultRemoteCallParameter, Object parameterValue) throws NotSerializableException;

}
