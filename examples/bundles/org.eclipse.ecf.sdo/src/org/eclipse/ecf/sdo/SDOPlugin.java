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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.ecf.core.ISharedObjectContainer;
import org.eclipse.ecf.core.util.ECFException;
import org.osgi.framework.BundleContext;

/**
 * (Temporary) entry point into the Shared Data Graph API. Serves as an
 * {@link org.eclipse.ecf.sdo.IDataGraphSharing IDataGraphSharing}factory.
 * 
 * @author pnehrer
 * @deprecated Use
 *             {@link org.eclipse.ecf.sdo.DataGraphSharingFactory DataGraphSharingFactory}
 *             instead.
 */
public class SDOPlugin extends Plugin {

    private static final String MANAGER_EXTENSION_POINT = "manager";

    private static final String MANAGER_EXTENSION = "manager";

    private static final String ATTR_NAME = "name";

    private static final String ATTR_CLASS = "class";

    // The shared instance.
    private static SDOPlugin plugin;

    private IRegistryChangeListener registryChangeListener;

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
        registryChangeListener = new IRegistryChangeListener() {
            public void registryChanged(IRegistryChangeEvent event) {
                IExtensionDelta[] deltas = event.getExtensionDeltas(getBundle()
                        .getSymbolicName(), MANAGER_EXTENSION_POINT);
                for (int i = 0; i < deltas.length; ++i) {
                    switch (deltas[i].getKind()) {
                    case IExtensionDelta.ADDED:
                        registerManagers(deltas[i].getExtension()
                                .getConfigurationElements());
                        break;

                    case IExtensionDelta.REMOVED:
                        IConfigurationElement[] elems = deltas[i]
                                .getExtension().getConfigurationElements();
                        for (int j = 0; j < elems.length; ++j) {
                            if (!MANAGER_EXTENSION.equals(elems[j].getName()))
                                continue;

                            String name = elems[j].getAttribute(ATTR_NAME);
                            if (name != null && name.length() > 0)
                                DataGraphSharingFactory.unregisterManager(name);
                        }

                        break;
                    }
                }
            }
        };

        IExtensionRegistry reg = Platform.getExtensionRegistry();
        IConfigurationElement[] elems = reg.getConfigurationElementsFor(
                getBundle().getSymbolicName(), MANAGER_EXTENSION_POINT);
        registerManagers(elems);
    }

    private void registerManagers(IConfigurationElement[] elems) {
        for (int i = 0; i < elems.length; ++i) {
            if (!MANAGER_EXTENSION.equals(elems[i].getName()))
                continue;

            String name = elems[i].getAttribute(ATTR_NAME);
            if (name == null || name.length() == 0)
                continue;

            IDataGraphSharingManager mgr;
            try {
                mgr = (IDataGraphSharingManager) elems[i]
                        .createExecutableExtension(ATTR_CLASS);
            } catch (Exception ex) {
                continue;
            }

            DataGraphSharingFactory.registerManager(name, mgr);
        }
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception {
        if (registryChangeListener != null)
            Platform.getExtensionRegistry().removeRegistryChangeListener(
                    registryChangeListener);

        DataGraphSharingFactory.unregisterAllManagers();
        super.stop(context);
    }

    /**
     * Returns the shared instance.
     */
    public static SDOPlugin getDefault() {
        return plugin;
    }

    /**
     * @param container
     * @return
     * @throws ECFException
     * @deprecated Use
     *             {@link DataGraphSharingFactory#getDataGraphSharing(ISharedObjectContainer, String) DataGraphSharingFactory.getDataGraphSharing(ISharedObjectContainer, String)}
     *             instead.
     */
    public IDataGraphSharing getDataGraphSharing(
            ISharedObjectContainer container) throws ECFException {

        return DataGraphSharingFactory
                .getDataGraphSharing(container, "default");
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