/*******************************************************************************
 * Copyright (c) 2004 Peter Nehrer and Composent, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Peter Nehrer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.sdo;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.ISharedObjectContainer;
import org.eclipse.ecf.core.ISharedObjectManager;
import org.eclipse.ecf.core.SharedObjectAddException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.IDInstantiationException;
import org.eclipse.ecf.internal.sdo.DataGraphSharing;
import org.osgi.framework.BundleContext;

/**
 * (Temporary) entry point into the Shared Data Graph API. Serves as an
 * {@link org.eclipse.ecf.sdo.IDataGraphSharing IDataGraphSharing}factory.
 * 
 * @author pnehrer
 */
public class SDOPlugin extends Plugin {

    // The shared instance.
    private static SDOPlugin plugin;

    private boolean debug;

    /**
     * The constructor.
     */
    public SDOPlugin() {
        super();
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
    }

    /**
     * Returns the shared instance.
     */
    public static SDOPlugin getDefault() {
        return plugin;
    }

    public IDataGraphSharing getDataGraphSharing(
            ISharedObjectContainer container) throws CoreException {

        ISharedObjectManager mgr = container.getSharedObjectManager();
        ID id;
        try {
            id = IDFactory.makeStringID(DataGraphSharing.DATA_GRAPH_SHARING_ID);
        } catch (IDInstantiationException e) {
            throw new CoreException(new Status(Status.ERROR, getBundle()
                    .getSymbolicName(), 0,
                    "Could not create data graph sharing ID.", e));
        }

        synchronized (container) {
            DataGraphSharing result = (DataGraphSharing) mgr
                    .getSharedObject(id);
            if (result == null) {
                result = new DataGraphSharing();
                result.setDebug(debug);
                try {
                    mgr.addSharedObject(id, result, null, null);
                } catch (SharedObjectAddException e) {
                    throw new CoreException(new Status(Status.ERROR,
                            getBundle().getSymbolicName(), 0,
                            "Could not add data sharing to the container.", e));
                }
            }

            return result;
        }
    }

    /**
     * Sets the debug flag.
     * 
     * @param debug
     * @deprecated Use Eclipse plug-in tracing support instead.
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

}