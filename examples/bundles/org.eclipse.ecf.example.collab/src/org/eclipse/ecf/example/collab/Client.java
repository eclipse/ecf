/****************************************************************************
* Copyright (c) 2004 Composent, Inc. and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Composent, Inc. - initial API and implementation
*****************************************************************************/

package org.eclipse.ecf.example.collab;

import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ecf.core.ISharedObjectContainer;
import org.eclipse.ecf.core.SharedObjectContainerFactory;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.example.collab.share.EclipseCollabSharedObject;
import org.eclipse.ecf.example.collab.share.SharedObjectEventListener;
import org.eclipse.ecf.example.collab.share.TreeItem;
import org.eclipse.ecf.example.collab.share.User;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class Client {
    private static final int CONTAINER_DISPOSE = 1000;
    public static final String JOIN_TIME_FORMAT = "hh:mm:ss a z";
    public static final String GENERIC_CONTAINER_CLIENT_NAME = "org.eclipse.ecf.provider.generic.Client";
    public static final String GENERIC_CONTAINER_SERVER_NAME = "org.eclipse.ecf.provider.generic.Server";
    public static final String DEFAULT_SERVER_ID = "ecftcp://localhost:3282/server";
    public static final String COLLAB_SHARED_OBJECT_ID = "chat";
    public static final String FILE_DIRECTORY = "received_files";
    public static final String USERNAME = System.getProperty("user.name");
    public static final String ECFDIRECTORY = "ECF_" + FILE_DIRECTORY + "/";
    static ID defaultGroupID = null;
    /*
    static ISharedObjectContainer client = null;
    static EclipseCollabSharedObject sharedObject = null;
    static ID groupID = null;
    static ID sharedObjectID = null;
    */
    static Hashtable clients = new Hashtable();

    public static class ClientEntry {
        String type;
        ISharedObjectContainer client;
        EclipseCollabSharedObject obj;
        
        public ClientEntry(String type, ISharedObjectContainer cont) {
            this.type = type;
            this.client = cont;
        }
        public String getType() {
            return type;
        }
        public ISharedObjectContainer getContainer() {
            return client;
        }
        public void setObject(EclipseCollabSharedObject obj) {
            this.obj = obj;
        }
        public EclipseCollabSharedObject getObject() {
            return obj;
        }
        public void dispose() {
            /*
            if (obj != null) {
                obj.destroySelf();
                obj = null;
            } 
            */
            /*
            if (client != null) {
                client.dispose(1000);
                client = null;
            }
            */
        }
    }
    
    protected static void addClientEntry(IProject proj, ClientEntry entry) {
        synchronized (clients) {
            Vector v = (Vector) clients.get(proj.getName());
            if (v == null) {
                v = new Vector();
            }
            v.add(entry);
            clients.put(proj.getName(),v);
        }
    }
    protected static Vector getClientEntries(IProject proj) {
        synchronized (clients) {
            return (Vector) clients.get(proj.getName());
        }
    }
    protected  static ClientEntry getClientEntry(IProject proj, String type) {
        synchronized (clients) {
            Vector v = (Vector) getClientEntries(proj);
            if (v == null) return null;
            for(Iterator i=v.iterator(); i.hasNext(); ) {
                ClientEntry e = (ClientEntry) i.next();
                if (isType(e,type)) {
                    return e;
                }
            }
        }
        return null;
    }
    protected static boolean containsEntry(IProject proj, String type) {
        synchronized (clients) {
            Vector v = (Vector) clients.get(proj.getName());
            if (v == null) return false;
            for(Iterator i=v.iterator(); i.hasNext(); ) {
                ClientEntry e = (ClientEntry) i.next();
                if (isType(e,type)) {
                    return true;
                }
            }
        }
        return false;
    }
    protected static void removeClientEntry(IProject proj, String type) {
        synchronized (clients) {
            Vector v = (Vector) clients.get(proj.getName());
            if (v == null) return;
            ClientEntry remove = null;
            for(Iterator i=v.iterator(); i.hasNext(); ) {
                ClientEntry e = (ClientEntry) i.next();
                if (isType(e,type)) {
                    remove = e;
                }
            }
            if (remove != null) v.remove(remove);
            if (v.size()==0) {
                clients.remove(proj.getName());
            }
        }
    }
    public Client() throws Exception {
        defaultGroupID = IDFactory.makeStringID(DEFAULT_SERVER_ID);
    }

    protected User getUserData(String containerType, ID clientID, String usernick, String proj) {
        Vector topElements = new Vector();
        String contType = containerType.substring(containerType.lastIndexOf(".")+1);
        topElements.add(new TreeItem("Project", proj));
        SimpleDateFormat sdf = new SimpleDateFormat(JOIN_TIME_FORMAT);
        topElements.add(new TreeItem("Join Time",sdf.format(new Date())));
        topElements.add(new TreeItem("Container Type",contType));
        return new User(clientID, usernick, topElements);
    }

    protected String getSharedFileDirectoryForProject(IProject proj) {
        String eclipseDir = Platform.getLocation().lastSegment();
        if (proj == null)
            return eclipseDir + "/" + ECFDIRECTORY;
        else return FILE_DIRECTORY;
    }

    protected IProject getFirstProjectFromWorkspace() throws Exception {
        IWorkspace ws = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot wr = ws.getRoot();
        IProject[] projects = wr.getProjects();
        if (projects == null)
            return null;
        return projects[0];
    }

    protected void makeAndAddSharedObject(final ClientEntry client,
            final IProject proj, User user, String fileDir) throws Exception {
        IWorkbenchWindow ww = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        EclipseCollabSharedObject sharedObject = new EclipseCollabSharedObject(proj, ww,
                user, fileDir);
        sharedObject.setListener(new SharedObjectEventListener() {
            public void memberRemoved(ID member) {
                ID groupID = client.getContainer().getGroupID();
                if (member.equals(groupID)) {
                    disposeClient(proj, client);
                }
            }
            public void memberAdded(ID member) {}
            public void otherActivated(ID other) {}
            public void otherDeactivated(ID other) {}
            public void windowClosing() {
                disposeClient(proj, client);
            }
        });
        ID newID = IDFactory.makeStringID(COLLAB_SHARED_OBJECT_ID);
        client.getContainer().getSharedObjectManager().addSharedObject(newID, sharedObject,
                new HashMap(), null);
        client.setObject(sharedObject);
    }

    protected void addObjectToClient(ClientEntry client,
            String username, IProject proj) throws Exception {
        IProject project = (proj == null) ? getFirstProjectFromWorkspace()
                : proj;
        String fileDir = getSharedFileDirectoryForProject(project);
        String projName = (project == null) ? "<workspace>" : project.getName();
        User user = getUserData(client.getClass().getName(),client.getContainer().getConfig().getID(),
                (username == null) ? USERNAME : username, projName);
        makeAndAddSharedObject(client, project, user, fileDir);
    }

    public synchronized ClientEntry isConnected(IProject project, String type) {
        if (type == null) type = GENERIC_CONTAINER_CLIENT_NAME;
        ClientEntry entry = getClientEntry(project,type);
        return entry;
    }

    public static boolean isType(ClientEntry entry, String type) {
        if (entry == null || type == null) return false;
        String name = entry.getType();
        if (name.equals(type)) return true;
        else return false;
    }
    public synchronized void createAndConnectClient(String type, ID gID, String username,
            Object data, IProject proj) throws Exception {
        
        if (proj == null) throw new NullPointerException("Project cannot be null");
        ClientEntry entry = getClientEntry(proj,type);
        if (entry != null) {
            // Already got a session going...that's OK as long as it's not of the same type...
                throw new ConnectException("Already connected");
        }
        
        String containerType = (type==null)?GENERIC_CONTAINER_CLIENT_NAME:type;
        ISharedObjectContainer client = SharedObjectContainerFactory
        .makeSharedObjectContainer(containerType);
        ClientEntry newClient = new ClientEntry(containerType,client);
        if (gID == null) {
            gID = defaultGroupID;
        } 
        if (containerType.equals(GENERIC_CONTAINER_CLIENT_NAME)) addObjectToClient(newClient, username, proj);
        client.joinGroup(gID, data);
        // only add client if the join successful
        addClientEntry(proj,newClient);
    }

    public synchronized void disposeClient(IProject proj, ClientEntry entry) {
        entry.dispose();
        removeClientEntry(proj,entry.getType());
    }

    public synchronized static ISharedObjectContainer getContainer(IProject proj) {
        ClientEntry entry = getClientEntry(proj,GENERIC_CONTAINER_CLIENT_NAME);
        if (entry != null) return entry.getContainer();
        else return null;
    }
}
