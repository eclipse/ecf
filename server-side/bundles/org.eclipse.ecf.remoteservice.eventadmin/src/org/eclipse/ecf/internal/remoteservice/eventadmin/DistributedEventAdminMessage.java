/****************************************************************************
 * Copyright (c) 2004, 2009 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.internal.remoteservice.eventadmin;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.osgi.util.NLS;
import org.osgi.service.event.Event;

public class DistributedEventAdminMessage implements Serializable {

	private static final long serialVersionUID = 2743430591605178391L;
	private String topic;
	private Map properties;

	private transient Event localEvent;

	public DistributedEventAdminMessage(Event event)
			throws NotSerializableException {
		this.topic = event.getTopic();
		this.properties = createPropertiesFromEvent(event);
	}

	private Map createPropertiesFromEvent(Event event)
			throws NotSerializableException {
		String[] propertyNames = event.getPropertyNames();
		Hashtable ht = (propertyNames == null) ? new Hashtable(1)
				: new Hashtable(propertyNames.length);
		for (int i = 0; i < propertyNames.length; i++) {
			Object val = event.getProperty(propertyNames[i]);
			if (!(val instanceof Serializable))
				throw new NotSerializableException(
						NLS
								.bind(
										"Cannot serialize property value of name={0} and value={1}",
										propertyNames[i], val));
			ht.put(propertyNames[i], val);
		}
		return ht;
	}

	public synchronized Event getEvent() {
		if (localEvent == null) {
			localEvent = new Event(topic, properties);
		}
		return localEvent;
	}
}
