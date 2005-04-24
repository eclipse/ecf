/*******************************************************************************
 * Copyright (c) 2005 Peter Nehrer and Composent, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Peter Nehrer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.internal.datashare;

import java.io.PrintStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.datashare.DataShareServiceFactory;
import org.eclipse.ecf.datashare.IDataShareServiceManager;
import org.osgi.framework.BundleContext;

/**
 * @author pnehrer
 * 
 */
public class DataSharePlugin {
    
	public static final String PLUGIN_ID = "org.eclipse.ecf.datashare";

    private static final String TRACE_PREFIX = PLUGIN_ID + "/";

    private static EclipsePlugin plugin;

    private static boolean tracingEnabled = Boolean.getBoolean(TRACE_PREFIX
            + "debug");

    private DataSharePlugin() {
    }

    public static void log(Object entry) {
        if (plugin == null) {
            if (entry instanceof Throwable)
                ((Throwable) entry).printStackTrace();
            else
                System.err.println(entry);
        } else {
            plugin.log(entry);
        }
    }

    public static boolean isTracing(String tag) {
        if (tracingEnabled) {
            return plugin == null ? Boolean.getBoolean(TRACE_PREFIX + tag)
                    : plugin.isTracing(tag);
        } else
            return false;
    }

    public static PrintStream getTraceLog() {
        return System.out;
    }

    public static class EclipsePlugin extends Plugin {

        private static final String MANAGER_EXTENSION_POINT = "manager";

        private static final String MANAGER_EXTENSION = "manager";

        private static final String ATTR_NAME = "name";

        private static final String ATTR_CLASS = "class";

        private IRegistryChangeListener registryChangeListener;

        public EclipsePlugin() {
            plugin = this;
            tracingEnabled = Platform.inDebugMode();
        }

        /**
         * This method is called upon plug-in activation
         */
        public void start(BundleContext context) throws Exception {
            super.start(context);
            registryChangeListener = new IRegistryChangeListener() {
                public void registryChanged(IRegistryChangeEvent event) {
                    IExtensionDelta[] deltas = event.getExtensionDeltas(
                            getBundle().getSymbolicName(),
                            MANAGER_EXTENSION_POINT);
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
                                if (!MANAGER_EXTENSION.equals(elems[j]
                                        .getName()))
                                    continue;

                                String name = elems[j].getAttribute(ATTR_NAME);
                                if (name != null && name.length() > 0)
                                    DataShareServiceFactory
                                            .unregisterManager(name);
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

                IDataShareServiceManager mgr;
                try {
                    mgr = (IDataShareServiceManager) elems[i]
                            .createExecutableExtension(ATTR_CLASS);
                } catch (Exception ex) {
                    continue;
                }

                DataShareServiceFactory.registerManager(name, mgr);
            }
        }

        /**
         * This method is called when the plug-in is stopped
         */
        public void stop(BundleContext context) throws Exception {
            if (registryChangeListener != null)
                Platform.getExtensionRegistry().removeRegistryChangeListener(
                        registryChangeListener);

            DataShareServiceFactory.unregisterAllManagers();
			plugin = null;
            super.stop(context);
        }

        public void log(Object entry) {
            IStatus status;
            if (entry instanceof IStatus)
                status = (IStatus) entry;
            else if (entry instanceof CoreException)
                status = ((CoreException) entry).getStatus();
            else if (entry instanceof Throwable) {
                Throwable t = (Throwable) entry;
                status = new Status(Status.ERROR,
                        getBundle().getSymbolicName(), 0,
                        t.getLocalizedMessage() == null ? "Unknown error." : t
                                .getLocalizedMessage(), t);
            } else
                status = new Status(Status.WARNING, getBundle()
                        .getSymbolicName(), 0, String.valueOf(entry),
                        new RuntimeException().fillInStackTrace());

            getLog().log(status);
        }

        public boolean isTracing(String tag) {
            return Boolean.TRUE.equals(Boolean.valueOf(Platform
                    .getDebugOption(TRACE_PREFIX + tag)));
        }
    }
}
