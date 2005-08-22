package org.eclipse.ecf.example.collab;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.ISharedObjectContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.security.ObjectCallback;
import org.eclipse.ecf.example.collab.share.EclipseCollabSharedObject;
import org.eclipse.ecf.presence.IPresenceContainer;

public class CollabClient {
	public static final String WORKSPACE_NAME = "<workspace>";
    public static final String GENERIC_CONTAINER_CLIENT_NAME = "org.eclipse.ecf.provider.generic.Client";
	static Hashtable clients = new Hashtable();
	static CollabClient collabClient = new CollabClient();

	/**
	 * Create a new container instance, and connect to a remote server or group.
	 * 
	 * @param containerType the container type used to create the new container instance.  Must not be null.
	 * @param uri the uri that is used to create a targetID for connection.  Must not be null.
	 * @param nickname an optional String nickname.  May be null.
	 * @param connectData optional connection data.  May be null.
	 * @param resource the resource that this container instance is associated with.  Must not be null.
	 * @throws Exception
	 */
	public void createAndConnectClient(final String containerType, String uri,
			String nickname, final Object connectData, final IResource resource)
			throws Exception {
		// Here we create the container instance
		final IContainer newClient = ContainerFactory
				.getDefault().makeContainer(containerType);
		// Create a new client entry to hold onto container once created
		final ClientEntry newClientEntry = new ClientEntry(containerType,
				newClient);
		
		// Then we get the target namespace, so we can create a target ID
		Namespace targetNamespace = newClient.getConnectNamespace();
		// Create the targetID instance
		ID targetID = IDFactory.getDefault().makeID(targetNamespace, uri);
		
		// Setup username
		String username = setupUsername(targetID,nickname);
		
		// Check for IPresenceContainer....if it is, setup presence UI, if not setup shared object container
		IPresenceContainer pc = (IPresenceContainer) newClient
				.getAdapter(IPresenceContainer.class);
		if (pc != null) {
			// Setup presence UI
			new PresenceContainerUI(pc).setup(newClient, targetID, username);
		} else {
			// Setup sharedobject container if the new instance supports this
			ISharedObjectContainer sharedObjectContainer = (ISharedObjectContainer) newClient
					.getAdapter(ISharedObjectContainer.class);
			if (sharedObjectContainer != null) {
				new SharedObjectContainerUI(this,sharedObjectContainer).setup(sharedObjectContainer,
						newClientEntry, resource, username);
			}
		}
		// Now connect
		try {
			newClient.connect(targetID, getJoinContext(username, connectData));
		} catch (ContainerConnectException e) {
			try {
				EclipseCollabSharedObject so = newClientEntry.getObject();
				if (so != null) {
					so.destroySelf();
				}
			} catch (Exception e1) {
			}
			throw e;
		}
		// only add client if the connect was successful
		addClientForResource(newClientEntry, resource);
	}

	public ClientEntry isConnected(IResource project, String type) {
		ClientEntry entry = getClientEntry(project, type);
		return entry;
	}

	protected static void addClientForResource(ClientEntry entry, IResource proj) {
		synchronized (clients) {
			String name = getNameForResource(proj);
			Vector v = (Vector) clients.get(name);
			if (v == null) {
				v = new Vector();
			}
			v.add(entry);
			clients.put(name, v);
		}
	}

	protected static void removeClientForResource(IResource proj, ID targetID) {
		synchronized (clients) {
			String resourceName = getNameForResource(proj);
			Vector v = (Vector) clients.get(resourceName);
			if (v == null)
				return;
			ClientEntry remove = null;
			for (Iterator i = v.iterator(); i.hasNext();) {
				ClientEntry e = (ClientEntry) i.next();
				ID connectedID = e.getConnectedID();
				if (connectedID == null || connectedID.equals(targetID)) {
					remove = e;
				}
			}
			if (remove != null)
				v.remove(remove);
			if (v.size() == 0) {
				clients.remove(resourceName);
			}
		}
	}

	public static String getNameForResource(IResource res) {
		String preName = res.getName().trim();
		if (preName == null || preName.equals("")) {
			preName = WORKSPACE_NAME;
		}
		return preName;
	}

	protected static IResource getWorkspace() throws Exception {
		IWorkspaceRoot ws = ResourcesPlugin.getWorkspace().getRoot();
		return ws;
	}

	protected static Vector getClientEntries(IResource proj) {
		synchronized (clients) {
			return (Vector) clients.get(getNameForResource(proj));
		}
	}

	protected static ClientEntry getClientEntry(IResource proj, ID targetID) {
		synchronized (clients) {
			Vector v = (Vector) getClientEntries(proj);
			if (v == null)
				return null;
			for (Iterator i = v.iterator(); i.hasNext();) {
				ClientEntry e = (ClientEntry) i.next();
				ID connectedID = e.getConnectedID();
				if (connectedID == null)
					continue;
				else if (connectedID.equals(targetID)) {
					return e;
				}
			}
		}
		return null;
	}

	protected static ClientEntry getClientEntry(IResource proj,
			String containerType) {
		synchronized (clients) {
			Vector v = (Vector) getClientEntries(proj);
			if (v == null)
				return null;
			for (Iterator i = v.iterator(); i.hasNext();) {
				ClientEntry e = (ClientEntry) i.next();
				ID connectedID = e.getConnectedID();
				if (connectedID == null)
					continue;
				else {
					String contType = e.getContainerType();
					if (contType.equals(containerType)) {
						return e;
					}
				}
			}
		}
		return null;
	}

	protected static boolean containsEntry(IResource proj, ID targetID) {
		synchronized (clients) {
			Vector v = (Vector) clients.get(getNameForResource(proj));
			if (v == null)
				return false;
			for (Iterator i = v.iterator(); i.hasNext();) {
				ClientEntry e = (ClientEntry) i.next();
				ID connectedID = e.getConnectedID();
				if (connectedID == null)
					continue;
				else if (connectedID.equals(targetID)) {
					return true;
				}
			}
		}
		return false;
	}
    public synchronized static ISharedObjectContainer getContainer(IResource proj) {
        ClientEntry entry = getClientEntry(proj,GENERIC_CONTAINER_CLIENT_NAME);
        if (entry == null) {
            entry = getClientEntry(ResourcesPlugin.getWorkspace().getRoot(),GENERIC_CONTAINER_CLIENT_NAME);
        }
        if (entry != null) {
        	IContainer cont = entry.getContainer();
        	if (cont != null) return (ISharedObjectContainer) cont.getAdapter(ISharedObjectContainer.class);
        	else return null;
        }
        else return null;
    }
	public static CollabClient getDefault() {
		return collabClient;
	}
	protected synchronized void disposeClient(IResource proj, ClientEntry entry) {
		entry.dispose();
		removeClientForResource(proj, entry.getConnectedID());
	}

	protected IConnectContext getJoinContext(final String username,
			final Object password) {
		return new IConnectContext() {
			public CallbackHandler getCallbackHandler() {
				return new CallbackHandler() {
					public void handle(Callback[] callbacks)
							throws IOException, UnsupportedCallbackException {
						if (callbacks == null)
							return;
						for (int i = 0; i < callbacks.length; i++) {
							if (callbacks[i] instanceof NameCallback) {
								NameCallback ncb = (NameCallback) callbacks[i];
								ncb.setName(username);
							} else if (callbacks[i] instanceof ObjectCallback) {
								ObjectCallback ocb = (ObjectCallback) callbacks[i];
								ocb.setObject(password);
							}
						}
					}
				};
			}
		};
	}
	protected String setupUsername(ID targetID, String nickname) throws URISyntaxException {
		String username = null;
		if (nickname != null) {
			username = nickname;
		} else {
			username = targetID.toURI().getUserInfo();
			if (username == null || username.equals(""))
				username = System.getProperty("user.name");
		}
		return username;
	}


}
