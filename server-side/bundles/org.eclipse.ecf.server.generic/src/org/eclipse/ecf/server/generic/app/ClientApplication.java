/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.server.generic.app;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Random;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.sharedobject.ISharedObject;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainer;
import org.eclipse.ecf.core.sharedobject.SharedObjectContainerFactory;
import org.eclipse.ecf.provider.generic.GenericContainerInstantiator;
import org.eclipse.ecf.provider.generic.TCPServerSOContainer;

/**
 * An ECF client container implementation that runs as an application.
 * <p>
 * Usage: java org.eclipse.ecf.provider.app.ClientApplication &lt;serverid&gt
 * <p>
 * If &lt;serverid&gt; is omitted or "-" is specified,
 * ecftcp://localhost:3282/server" is used.
 * 
 */
public class ClientApplication {

	public static final int DEFAULT_WAITTIME = 40000;

	public static final int DEFAULT_TIMEOUT = TCPServerSOContainer.DEFAULT_KEEPALIVE;

	public static final String CONTAINER_FACTORY_NAME = GenericContainerInstantiator.class.getName();
	public static final String CONTAINER_FACTORY_CLASS = CONTAINER_FACTORY_NAME;

	public static final String COMPOSENT_CONTAINER_NAME = GenericContainerInstantiator.class.getName();

	// Number of clients to create
	static int clientCount = 1;
	// Array of client instances
	ISharedObjectContainer[] sm = new ISharedObjectContainer[clientCount];
	// ServerApplication name to connect to
	String serverName = null;
	// Class names of any sharedObjects to be created. If null, no sharedObjects
	// created.
	String[] sharedObjectClassNames = null;
	// IDs of sharedObjects created
	ID[] sharedObjects = null;

	static ContainerTypeDescription contd = null;
	static Random aRan = new SecureRandom();

	public ClientApplication() {
		super();
	}

	public void init(String[] args) throws Exception {
		serverName = TCPServerSOContainer.getDefaultServerURL();
		if (args.length > 0) {
			if (!args[0].equals("-"))serverName = args[0]; //$NON-NLS-1$
		}
		if (args.length > 1) {
			sharedObjectClassNames = new String[args.length - 1];
			for (int i = 0; i < args.length - 1; i++) {
				sharedObjectClassNames[i] = args[i + 1];
			}
		}
		// Setup factory descriptions since Eclipse does not do this for us
		contd = new ContainerTypeDescription(CONTAINER_FACTORY_NAME, CONTAINER_FACTORY_CLASS, null);
		ContainerFactory.getDefault().addDescription(contd);
		for (int i = 0; i < clientCount; i++) {
			sm[i] = createClient();
		}
	}

	protected ISharedObjectContainer createClient() throws Exception {
		// Make identity instance for the new container
		ID newContainerID = IDFactory.getDefault().createGUID();
		ISharedObjectContainer result = SharedObjectContainerFactory.getDefault().createSharedObjectContainer(contd, new Object[] {newContainerID, Integer.valueOf(DEFAULT_TIMEOUT)});
		return result;
	}

	public void connect(ID server) throws Exception {
		for (int i = 0; i < clientCount; i++) {
			System.out.print("ClientApplication " + sm[i].getID().getName() + " joining " + server.getName() + "..."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			sm[i].connect(server, null);
			System.out.println("completed."); //$NON-NLS-1$
		}
	}

	public void disconnect() {
		for (int i = 0; i < clientCount; i++) {
			System.out.print("ClientApplication " + sm[i].getID().getName() + " leaving..."); //$NON-NLS-1$ //$NON-NLS-2$
			sm[i].disconnect();
			System.out.println("completed."); //$NON-NLS-1$
		}
	}

	public void createSharedObjects() throws Exception {
		if (sharedObjectClassNames != null) {
			for (int j = 0; j < clientCount; j++) {
				ISharedObjectContainer scg = sm[j];
				sharedObjects = new ID[sharedObjectClassNames.length];
				for (int i = 0; i < sharedObjectClassNames.length; i++) {
					System.out.println("Creating sharedObject: " + sharedObjectClassNames[i] + " for client " + scg.getID().getName()); //$NON-NLS-1$ //$NON-NLS-2$
					ISharedObject so = (ISharedObject) Class.forName(sharedObjectClassNames[i]).newInstance();
					sharedObjects[i] = IDFactory.getDefault().createStringID(sharedObjectClassNames[i] + "_" + i); //$NON-NLS-1$
					scg.getSharedObjectManager().addSharedObject(sharedObjects[i], so, new HashMap());
					System.out.println("Created sharedObject for client " + scg.getID().getName()); //$NON-NLS-1$
				}
			}
		}

	}

	public void removeSharedObjects() throws Exception {
		if (sharedObjects == null)
			return;
		for (int j = 0; j < clientCount; j++) {
			for (int i = 0; i < sharedObjects.length; i++) {
				System.out.println("Removing sharedObject: " + sharedObjects[i].getName() + " for client " + sm[j].getID().getName()); //$NON-NLS-1$ //$NON-NLS-2$
				sm[j].getSharedObjectManager().removeSharedObject(sharedObjects[i]);
			}
		}
	}

	/**
	 * An ECF client container implementation that runs as an application.
	 * <p>
	 * Usage: java org.eclipse.ecf.provider.app.ClientApplication
	 * &lt;serverid&gt
	 * <p>
	 * If &lt;serverid&gt; is omitted or "-" is specified,
	 * ecftcp://localhost:3282/server" is used.
	 * 
	 */
	public static void main(String[] args) throws Exception {
		ClientApplication st = new ClientApplication();
		st.init(args);
		// Get server id to join
		ID serverID = IDFactory.getDefault().createStringID(st.serverName);
		st.connect(serverID);
		st.createSharedObjects();
		System.out.println("Waiting " + DEFAULT_WAITTIME + " ms..."); //$NON-NLS-1$ //$NON-NLS-2$
		Thread.sleep(DEFAULT_WAITTIME);
		st.removeSharedObjects();
		st.disconnect();
		System.out.println("Exiting."); //$NON-NLS-1$
	}

}
