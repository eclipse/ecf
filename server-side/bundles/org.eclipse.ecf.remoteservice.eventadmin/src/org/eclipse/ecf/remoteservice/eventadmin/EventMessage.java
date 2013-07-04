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
package org.eclipse.ecf.remoteservice.eventadmin;

import java.io.Externalizable;
import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.ecf.internal.remoteservice.eventadmin.NoSerializationHandler;
import org.eclipse.ecf.remoteservice.eventadmin.serialization.SmartSerializationHandler;
import org.osgi.service.event.Event;

/**
 * @since 1.1
 */
public class EventMessage implements Serializable {

	private static final long serialVersionUID = 2743430591605178391L;
	
	/**
	 * @since 1.2
	 */
	public static SmartSerializationHandler serializationHandler = new NoSerializationHandler();
	
	private String topic;
	private Map properties;

	private transient Event localEvent;

	public EventMessage(Event event)
			throws NotSerializableException {
		this.topic = event.getTopic();
		this.properties = createPropertiesFromEvent(event);
	}

	public EventMessage(String topic, Map properties) {
		this.topic = topic;
		this.properties = properties;
	}
	
	protected Map createPropertiesFromEvent(Event event)
			throws NotSerializableException {
		String[] propertyNames = event.getPropertyNames();
		Hashtable ht = (propertyNames == null) ? new Hashtable(1)
				: new Hashtable(propertyNames.length);
		for (int i = 0; i < propertyNames.length; i++) {
			Object val = event.getProperty(propertyNames[i]);
			if (!(val instanceof Serializable || val instanceof Externalizable)) {
				val = serializationHandler.serialize(val);
			}
			ht.put(propertyNames[i], val);
		}
		return ht;
	}

	protected String getTopic() {
		return topic;
	}
	
	protected Map getProperties() {
//		if (useSmartSerialization) {
//			// check to see if there is a smart serialized object contained in
//			// the props
//			for (int i = 0; i < properties.size(); i++) {
//				
//			}
//			return properties;
//		} else {
			return properties;
//		}
	}
	
	protected Event createLocalEvent() {
		return new Event(getTopic(), getProperties());
	}
	
	public synchronized Event getEvent() {
		if (localEvent == null) {
			localEvent = createLocalEvent();
		}
		return localEvent;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("EventMessage[topic=");
		buffer.append(topic);
		buffer.append(", properties=");
		buffer.append(properties);
		buffer.append("]");
		return buffer.toString();
	}
	
}
