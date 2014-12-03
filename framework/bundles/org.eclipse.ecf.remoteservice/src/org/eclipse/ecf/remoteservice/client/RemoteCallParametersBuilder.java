/*******************************************************************************
* Copyright (c) 2014 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.remoteservice.client;

import java.util.*;

/**
 * @since 8.5
 */
public class RemoteCallParametersBuilder {

	private final Map<String, Object> nameDefaultValueMap;

	public RemoteCallParametersBuilder() {
		this.nameDefaultValueMap = new HashMap<String, Object>();
	}

	public RemoteCallParametersBuilder addParameter(String name, Object defaultValue) {
		this.nameDefaultValueMap.put(name, defaultValue);
		return this;
	}

	public RemoteCallParametersBuilder addParameter(String name) {
		return addParameter(name, null);
	}

	public IRemoteCallParameter[] build() {
		List<IRemoteCallParameter> params = new ArrayList<IRemoteCallParameter>();
		for (String name : this.nameDefaultValueMap.keySet()) {
			Object value = this.nameDefaultValueMap.get(name);
			params.add(((value == null) ? new RemoteCallParameter(name) : new RemoteCallParameter(name, value)));
		}

		return (params.size() == 0) ? null : params.toArray(new IRemoteCallParameter[params.size()]);
	}
}
