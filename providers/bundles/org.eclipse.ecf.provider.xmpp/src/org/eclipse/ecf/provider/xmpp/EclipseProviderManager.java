/*******************************************************************************
 * Copyright (c) 2004, 2005 Jean-Michel Lemieux, Jeff McAffer and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Hyperbola is an RCP application developed for the book
 *     Eclipse Rich Client Platform -
 *         Designing, Coding, and Packaging Java Applications
 *         (http://eclipsercp.org)
 * Contributors:
 *     Jean-Michel Lemieux and Jeff McAffer - initial implementation
 *******************************************************************************/
package org.eclipse.ecf.provider.xmpp;

import java.util.ArrayList;
import java.util.Iterator;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.dynamichelpers.*;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.*;

/**
 * Manages providers for parsing custom XML sub-documents of XMPP packets. Two types of
 * providers exist:<ul>
 *      <li>IQProvider -- parses IQ requests into Java objects.
 *      <li>PacketExtension -- parses XML sub-documents attached to packets into
 *          PacketExtension instances.</ul>
 *
 * <b>IQProvider</b><p>
 *
 * By default, Smack only knows how to process IQ packets with sub-packets that
 * are in a few namespaces such as:<ul>
 *      <li>jabber:iq:auth
 *      <li>jabber:iq:roster
 *      <li>jabber:iq:register</ul>
 *
 * Because many more IQ types are part of XMPP and its extensions, a pluggable IQ parsing
 * mechanism is provided. IQ providers are registered programatically or by creating an
 * extension to the org.eclipse.jabber.ipProviders or org.eclipse.jabber.extensionProviders 
 * extension points as appropriate.
 * 
 */
public class EclipseProviderManager extends ProviderManager implements IExtensionChangeHandler {
	public static final String IQ = "iq";
	public static final String EXTENSION = "extension";

	private IExtensionTracker tracker;
	private ArrayList directories;

	public EclipseProviderManager() {
		super();
	}

	/**
	 * Returns the IQ provider registered to the specified XML element name and namespace.
	 * For example, if a provider was registered to the element name "query" and the
	 * namespace "jabber:iq:time", then the following packet would trigger the provider:
	 *
	 * <pre>
	 * &lt;iq type='result' to='joe@example.com' from='mary@example.com' id='time_1'&gt;
	 *     &lt;query xmlns='jabber:iq:time'&gt;
	 *         &lt;utc&gt;20020910T17:58:35&lt;/utc&gt;
	 *         &lt;tz&gt;MDT&lt;/tz&gt;
	 *         &lt;display&gt;Tue Sep 10 12:58:35 2002&lt;/display&gt;
	 *     &lt;/query&gt;
	 * &lt;/iq&gt;</pre>
	 *
	 * <p>Note: this method is generally only called by the internal Smack classes.
	 *
	 * @param elementName the XML element name.
	 * @param namespace the XML namespace.
	 * @return the IQ provider.
	 */
	public Object getIQProvider(String elementName, String namespace) {
		return getProvider(elementName, namespace, IQ, true);
	}

	/**
	 * Returns the packet extension provider registered to the specified XML element name
	 * and namespace. For example, if a provider was registered to the element name "x" and the
	 * namespace "jabber:x:event", then the following packet would trigger the provider:
	 *
	 * <pre>
	 * &lt;message to='romeo@montague.net' id='message_1'&gt;
	 *     &lt;body&gt;Art thou not Romeo, and a Montague?&lt;/body&gt;
	 *     &lt;x xmlns='jabber:x:event'&gt;
	 *         &lt;composing/&gt;
	 *     &lt;/x&gt;
	 * &lt;/message&gt;</pre>
	 *
	 * <p>Note: this method is generally only called by the internal Smack classes.
	 *
	 * @param elementName
	 * @param namespace
	 * @return the extenion provider.
	 */
	public Object getExtensionProvider(String elementName, String namespace) {
		return getProvider(elementName, namespace, EXTENSION, true);
	}

	public void start() {
		initializeTracker();
		directories = new ArrayList(11);
		readDirectories();
	}

	public void stop() {
		directories = null;
		tracker.close();
	}

	/**
	 * @param elementName
	 * @param namespace
	 * @return
	 */
	private Object getProvider(String elementName, String namespace, String type, boolean tryFetch) {
		Object result = null;
		if (type == IQ)
			result = super.getIQProvider(elementName, namespace);
		else if (type == EXTENSION)
			result = super.getExtensionProvider(elementName, namespace);
		if (result != null)
			return result;

		result = getProviderExtension(elementName, namespace, type);
		if (result != null)
			return result;

		if (!tryFetch)
			return null;
		fetchProvider(elementName, namespace, type);
		return getProvider(elementName, namespace, type, false);
	}

	/**
	 * @param elementName
	 * @param namespace
	 * @return
	 */
	private Object getProviderExtension(String elementName, String namespace, String type) {
		String point = type == IQ ? (XmppPlugin.ID+"iqProviders") : (XmppPlugin.ID+".extensionProviders");
		IConfigurationElement[] decls = Platform.getExtensionRegistry().getConfigurationElementsFor(point);
		for (int i = 0; i < decls.length; i++) {
			IConfigurationElement element = decls[i];
			if (elementName.equals(element.getAttribute("elementName")) && namespace.equals(element.getAttribute("namespace"))) {
				try {
					Object provider = element.createExecutableExtension("className");
					tracker.registerObject(element.getDeclaringExtension(), provider, IExtensionTracker.REF_SOFT);
					return addProvider(elementName, namespace, type, provider);
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * @param elementName
	 * @param namespace
	 * @param iq
	 */
	private void fetchProvider(String elementName, String namespace, String type) {
		for (Iterator iter = directories.iterator(); iter.hasNext();) {
			IProviderDirectory element = (IProviderDirectory) iter.next();
			IBundleGroup group = element.getProvider(elementName, namespace, type);
			if (group != null) {
				element.installProvider(group);
				return;
			}
		}
	}

	/**
	 * @param elementName
	 * @param namespace
	 * @param type
	 * @param provider
	 */
	private Object addProvider(String elementName, String namespace, String type, Object provider) {
		if (type == EXTENSION) {
			if (PacketExtensionProvider.class.isAssignableFrom(provider.getClass())) {
				addExtensionProvider(elementName, namespace, provider);
			}
			if (PacketExtension.class.isAssignableFrom(provider.getClass())) {
				addExtensionProvider(elementName, namespace, provider.getClass());
			}
		} else if (type == IQ) {
			if (IQProvider.class.isAssignableFrom(provider.getClass())) {
				addIQProvider(elementName, namespace, provider);
				return provider;
			}
			if (IQ.class.isAssignableFrom(provider.getClass())) {
				addIQProvider(elementName, namespace, provider.getClass());
				return provider.getClass();
			}
		}
		return provider;
	}
	
	private void removeProvider(Object provider) {
		// dummy method for now.
	}

	private void readDirectories() {
		IConfigurationElement[] decls = Platform.getExtensionRegistry().getConfigurationElementsFor(XmppPlugin.ID+".providerDirectories");
		for (int i = 0; i < decls.length; i++) {
			IConfigurationElement element = decls[i];
			try {
				// TODO: nitpick, but we may want to at least talk about lazy plug-in loading with respect to 
				// extension points. This is an example of the oposite :) 
				Object directory = element.createExecutableExtension("className");
				tracker.registerObject(element.getDeclaringExtension(), directory, IExtensionTracker.REF_SOFT);
				directories.add(directory);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	private void initializeTracker() {
		tracker = new ExtensionTracker();
		String[] points = new String[] {"iqProviders", "extensionProviders", "directoryProviders"};
		for (int i = 0; i < points.length; i++) {
			IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(XmppPlugin.ID, points[i]);
			if (point != null) {
				IExtension[] extensions = point.getExtensions();
				for (int j = 0; j < extensions.length; j++)
					addExtension(tracker, extensions[j]);
			}
		}
		tracker.registerHandler(this, ExtensionTracker.createNamespaceFilter(XmppPlugin.ID));
	}

	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		// new extensions are accessed on demand.
	}

	public void removeExtension(IExtension extension, Object[] objects) {
		for (int i = 0; i < objects.length; i++) {
			Object object = objects[i];
			directories.remove(object);
			removeProvider(object);
		}
	}

}