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
package org.eclipse.ecf.pubsub.impl;

import java.io.Serializable;
import java.util.Arrays;

import org.eclipse.ecf.pubsub.PublishedServiceDescriptor;

public class DiscoveryMessage implements Serializable {

	private static final long serialVersionUID = 2321101436711728754L;

	public static final int ADDED = 0;
	
	public static final int REMOVED = 1;
	
	private final int kind;
	
	private final PublishedServiceDescriptor[] descriptors;
	
	public DiscoveryMessage(int kind, PublishedServiceDescriptor[] descriptors) {
		this.kind = kind;
		this.descriptors = descriptors;
	}

	public DiscoveryMessage(int kind, PublishedServiceDescriptor descriptor) {
		this(kind, new PublishedServiceDescriptor[] { descriptor });
	}
	
	public int getKind() {
		return kind;
	}

	public PublishedServiceDescriptor[] getDescriptors() {
		return descriptors;
	}

	public int hashCode() {
		int c = 17;
		c = 37 * c + kind;
		c = 37 * c + descriptors[0].hashCode();
		return c;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;

		DiscoveryMessage other = (DiscoveryMessage) obj;
		return kind == other.kind && Arrays.equals(descriptors, other.descriptors);
	}
}