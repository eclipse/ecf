/******************************************************************************* 
 * Copyright (c) 2010-2011 Naumen. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Pavel Samolisov - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.remoteservice.rpc.client;

import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.client.*;

/**
 * Trivial parameter serializer - just copy a parameter value
 * 
 * @author psamolisov
 */
public class TrivialParameterServializer implements IRemoteCallParameterSerializer {

	/**
	 * All parameters will be serialized in the Apache XML-RPC library. We shouldn't serialize any parameters
	 * by default. 
	 * 
	 * @return the parameter value
	 */
	public IRemoteCallParameter serializeParameter(String endpoint, IRemoteCall call, IRemoteCallable callable,
			IRemoteCallParameter paramDefault, Object paramToSerialize) {
		// Just return a parameter		
		return new RemoteCallParameter(paramDefault.getName(), paramToSerialize == null ? paramDefault.getValue()
				: paramToSerialize);
	}
}
