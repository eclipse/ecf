/****************************************************************************
 * Copyright (c) 2006 Ecliptical Software Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Ecliptical Software Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.pubsub;

import java.util.Arrays;
import java.util.EventObject;

public class PublishedServiceDirectoryChangeEvent extends EventObject {
	
	private static final long serialVersionUID = 5748843360974872790L;
	
	public static final int ADDED = 0;
	
	public static final int REMOVED = 1;
	
	private final int kind;
	
	private final PublishedServiceDescriptor[] services;

	public PublishedServiceDirectoryChangeEvent(IPublishedServiceDirectory manager, int kind, PublishedServiceDescriptor[] services) {
		super(manager);
		this.kind = kind;
		this.services = services;
	}
	
	public IPublishedServiceDirectory getDirectory() {
		return (IPublishedServiceDirectory) source;
	}
	
	public int getKind() {
		return kind;
	}

	public PublishedServiceDescriptor[] getReplicatedServices() {
		return services;
	}

	public int hashCode() {
		int c = 17;
		c = 37 * c + source.hashCode();
		c = 37 * c + kind;
		c = 37 * c + services[0].hashCode();
		return c;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;
		
		PublishedServiceDirectoryChangeEvent other = (PublishedServiceDirectoryChangeEvent) obj;
		return source.equals(other.source) && kind == other.kind && Arrays.equals(services, other.services);
	}
}
