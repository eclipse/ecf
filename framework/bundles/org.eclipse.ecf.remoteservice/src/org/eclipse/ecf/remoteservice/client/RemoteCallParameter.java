/*******************************************************************************
* Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.remoteservice.client;

import java.util.*;
import org.eclipse.core.runtime.Assert;

/**
 * Implementation of {@link IRemoteCallParameter}.
 * 
 * @since 4.0
 */
public class RemoteCallParameter implements IRemoteCallParameter {

	private String name;
	private Object value;

	public RemoteCallParameter(String name, Object value) {
		this.name = name;
		Assert.isNotNull(this.name);
		this.value = value;
	}

	public RemoteCallParameter(String name) {
		this(name, null);
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("RemoteCallParameter[name="); //$NON-NLS-1$
		buffer.append(name);
		buffer.append(", value="); //$NON-NLS-1$
		buffer.append(value);
		buffer.append("]"); //$NON-NLS-1$
		return buffer.toString();
	}

	/**
	 * @since 8.5
	 */
	public static class Builder {
		private final Map<String, Object> nameDefaultValueMap;

		public Builder() {
			this.nameDefaultValueMap = new HashMap<String, Object>();
		}

		public Builder addParameter(String name, Object defaultValue) {
			this.nameDefaultValueMap.put(name, defaultValue);
			return this;
		}

		public Builder addParameter(String name) {
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
}
