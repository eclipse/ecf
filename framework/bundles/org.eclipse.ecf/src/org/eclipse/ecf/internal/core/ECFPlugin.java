/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.core;

import java.lang.reflect.Constructor;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.SharedObjectContainerDescription;
import org.eclipse.ecf.core.SharedObjectContainerFactory;
import org.eclipse.ecf.core.comm.ConnectionDescription;
import org.eclipse.ecf.core.comm.ConnectionFactory;
import org.eclipse.ecf.core.comm.provider.ISynchAsynchConnectionInstantiator;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.identity.provider.IDInstantiator;
import org.eclipse.ecf.core.provider.ISharedObjectContainerInstantiator;
import org.osgi.framework.BundleContext;

public class ECFPlugin extends Plugin {
    public static final String PLUGIN_ID = "org.eclipse.ecf";
    public static final Trace trace = Trace.create("factoryinit");
    public static final String PLUGIN_RESOURCE_BUNDLE = "org.eclipse.ecf.ECFPluginResources";
    public static final String NAMESPACE_EPOINT = "org.eclipse.ecf.namespace";
    public static final String INSTANTIATOR_CLASS_ATTRIBUTE = "instantiatorClass";
    public static final String INSTANTIATOR_NAME_ATTRIBUTE = "name";
    public static final String NAMESPACE_CLASS_ATTRIBUTE = "namespaceClass";
    public static final String INSTANTIATOR_DATA_ATTRIBUTE = "description";
    public static final String NAMESPACE_DEFAULT_CLASS = "org.eclipse.ecf.core.identity.Namespace";
    public static final String CONTAINER_FACTORY_EPOINT = "org.eclipse.ecf.containerFactory";
    public static final String CONTAINER_FACTORY_EPOINT_CLASS_ATTRIBUTE = "class";
    public static final String CONTAINER_FACTORY_EPOINT_NAME_ATTRIBUTE = "name";
    public static final String CONTAINER_FACTORY_EPOINT_DESC_ATTRIBUTE = "description";
    public static final String ARG_ELEMENT_NAME = "defaultargument";
    public static final String ARG_TYPE_ATTRIBUTE = "type";
    public static final String ARG_VALUE_ATTRIBUTE = "value";
    public static final String ARG_NAME_ATTRIBUTE = "name";
    public static final String COMM_FACTORY_EPOINT = "org.eclipse.ecf.connectionFactory";
    public static final String COMM_FACTORY_EPOINT_CLASS_ATTRIBUTE = "class";
    public static final String COMM_FACTORY_EPOINT_NAME_ATTRIBUTE = "name";
    public static final String COMM_FACTORY_EPOINT_DESC_ATTRIBUTE = "description";
    public static final int FACTORY_DOES_NOT_IMPLEMENT_ERRORCODE = 10;
    public static final int FACTORY_NAME_COLLISION_ERRORCODE = 20;
    public static final int INSTANTIATOR_DOES_NOT_IMPLEMENT_ERRORCODE = 30;
    public static final int INSTANTIATOR_NAME_COLLISION_ERRORCODE = 50;
    public static final int INSTANTIATOR_NAMESPACE_LOAD_ERRORCODE = 60;
    //The shared instance.
    private static ECFPlugin plugin;
    //Resource bundle.
    private ResourceBundle resourceBundle;
    BundleContext context = null;

    private static void debug(String msg) {
        if (Trace.ON && trace != null) {
            trace.msg(msg);
        }
    }

    private static void dumpStack(String msg, Throwable e) {
        if (Trace.ON && trace != null) {
            trace.dumpStack(e, msg);
        }
    }

    public ECFPlugin() {
        super();
        debug("<init>");
        plugin = this;
        try {
            resourceBundle = ResourceBundle.getBundle(PLUGIN_RESOURCE_BUNDLE);
        } catch (MissingResourceException x) {
            resourceBundle = null;
        }
    }

    public static void log(IStatus status) {
        if (status == null)
            return;
        ILog log = plugin.getLog();
        if (log != null) {
            log.log(status);
        } else {
            System.err.println("No log output.  Status Message: "
                    + status.getMessage());
        }
    }

    class DefaultArgs {
        String[] types;
        String[] defaults;
        String[] names;

        public DefaultArgs(String[] types, String[] defaults, String[] names) {
            this.types = types;
            this.defaults = defaults;
            this.names = names;
        }

        public String[] getDefaults() {
            return defaults;
        }

        public String[] getNames() {
            return names;
        }

        public String[] getTypes() {
            return types;
        }
    }

    protected DefaultArgs getDefaultArgs(IConfigurationElement[] argElements) {
        String[] argTypes = new String[0];
        String[] argDefaults = new String[0];
        String[] argNames = new String[0];
        if (argElements != null) {
            if (argElements.length > 0) {
                argTypes = new String[argElements.length];
                argDefaults = new String[argElements.length];
                argNames = new String[argElements.length];
                for (int i = 0; i < argElements.length; i++) {
                    argTypes[i] = argElements[i]
                            .getAttribute(ARG_TYPE_ATTRIBUTE);
                    argDefaults[i] = argElements[i]
                            .getAttribute(ARG_VALUE_ATTRIBUTE);
                    argNames[i] = argElements[i]
                            .getAttribute(ARG_NAME_ATTRIBUTE);
                }
            }
        }
        return new DefaultArgs(argTypes, argDefaults, argNames);
    }

    protected void setupContainerExtensionPoint(BundleContext bc) {
        String bundleName = getDefault().getBundle().getSymbolicName();
        IExtensionRegistry reg = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = reg
                .getExtensionPoint(CONTAINER_FACTORY_EPOINT);
        if (extensionPoint == null) {
            return;
        }
        IConfigurationElement[] members = extensionPoint
                .getConfigurationElements();
        // For each configuration element
        for (int m = 0; m < members.length; m++) {
            debug("setupContainerExtensionPoint:" + members[m].getName());
            IConfigurationElement member = members[m];
            // Get the label of the extender plugin and the ID of the extension.
            IExtension extension = member.getDeclaringExtension();
            Object exten = null;
            String name = null;
            try {
                // The only required attribute is "class"
                exten = member
                        .createExecutableExtension(CONTAINER_FACTORY_EPOINT_CLASS_ATTRIBUTE);
                ClassLoader cl = exten.getClass().getClassLoader();
                String clazz = exten.getClass().getName();
                // Get name and get version, if available
                name = member
                        .getAttribute(CONTAINER_FACTORY_EPOINT_NAME_ATTRIBUTE);
                if (name == null) {
                    name = clazz;
                }
                String description = member
                        .getAttribute(CONTAINER_FACTORY_EPOINT_DESC_ATTRIBUTE);
                if (description == null) {
                    description = "";
                }
                // Get any arguments
                DefaultArgs defaults = getDefaultArgs(member
                        .getChildren(ARG_ELEMENT_NAME));
                // Now make description instance
                SharedObjectContainerDescription scd = new SharedObjectContainerDescription(
                        name, (ISharedObjectContainerInstantiator) exten,
                        description, defaults.getTypes(), defaults
                                .getDefaults(), defaults.getNames());
                debug("setupContainerExtensionPoint:created description:" + scd);
                if (SharedObjectContainerFactory.containsDescription(scd)) {
                    throw new CoreException(getStatusForContException(extension,bundleName,name));
                }
                // Now add the description and we're ready to go.
                SharedObjectContainerFactory.addDescription(scd);
                debug("setupContainerExtensionPoint:added description to factory:"
                        + scd);
            } catch (CoreException e) {
                log(e.getStatus());
                dumpStack("CoreException in setupContainerExtensionPoint", e);
            } catch (Exception e) {
                log(getStatusForContException(extension, bundleName, name));
                dumpStack("Exception in setupContainerExtensionPoint", e);
            }
        }
    }

    protected void setupIdentityExtensionPoint(BundleContext context) {
        String bundleName = getDefault().getBundle().getSymbolicName();
        // Process extension points
        IExtensionRegistry reg = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = reg
                .getExtensionPoint(NAMESPACE_EPOINT);
        if (extensionPoint == null) {
            return;
        }
        IConfigurationElement[] members = extensionPoint
                .getConfigurationElements();
        // For each service:
        for (int m = 0; m < members.length; m++) {
            debug("setupIdentityExtensionPoint:" + members[m].getName());
            IConfigurationElement member = members[m];
            // Get the label of the extender plugin and the ID of the
            // extension.
            IExtension extension = member.getDeclaringExtension();
            String nsName = null;
            try {
                String nsInstantiatorClass = member
                        .getAttribute(INSTANTIATOR_CLASS_ATTRIBUTE);
                if (nsInstantiatorClass == null) {
                    throw new CoreException(null);
                }
                nsName = member
                        .getAttribute(INSTANTIATOR_NAME_ATTRIBUTE);
                if (nsName == null) {
                    nsName = nsInstantiatorClass;
                }
                String nsClassName = member
                        .getAttribute(NAMESPACE_CLASS_ATTRIBUTE);
                if (nsClassName == null) {
                    nsClassName = NAMESPACE_DEFAULT_CLASS;
                }
                String nsData = member
                        .getAttribute(INSTANTIATOR_DATA_ATTRIBUTE);
                // Load instantiator class and create instance
                Object obj = member
                        .createExecutableExtension(INSTANTIATOR_CLASS_ATTRIBUTE);
                // Verify that object implements IDInstantiator
                ClassLoader loader = obj.getClass().getClassLoader();
                Namespace ns = null;
                try {
                    Class nsClass = Class.forName(nsClassName, true, loader);
                    Constructor cons = nsClass
                            .getDeclaredConstructor(new Class[] { String.class,
                                    IDInstantiator.class, String.class });
                    ns = (Namespace) cons.newInstance(new Object[] { nsName,
                            obj, nsData });
                } catch (Exception e) {
                    IStatus s = new Status(
                            Status.ERROR,
                            bundleName,
                            INSTANTIATOR_NAMESPACE_LOAD_ERRORCODE,
                            getResourceString("ExtPointError.IDNameLoadErrorPrefix")
                                    + nsName
                                    + getResourceString("ExtPointError.IDNameLoadErrorSuffix")
                                    + extension
                                            .getExtensionPointUniqueIdentifier(),
                            e);
                    throw new CoreException(s);
                }
                debug("setupIdentityExtensionPoint:created namespace:" + ns);
                if (IDFactory.containsNamespace(ns)) {
                    throw new CoreException(getStatusForIDException(extension,bundleName,nsName));
                }
                // Now add to known namespaces
                IDFactory.addNamespace(ns);
                debug("setupIdentityExtensionPoint:added namespace to factory:"
                        + ns);
            } catch (CoreException e) {
                log(e.getStatus());
                dumpStack("Exception in setupIdentityExtensionPoint", e);
            } catch (Exception e) {
                log(getStatusForIDException(extension,bundleName,nsName));
                dumpStack("Exception in setupIdentityExtensionPoint",e);
            }
        }
    }

    protected void setupCommExtensionPoint(BundleContext bc) {
        String bundleName = getDefault().getBundle().getSymbolicName();
        IExtensionRegistry reg = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = reg
                .getExtensionPoint(COMM_FACTORY_EPOINT);
        if (extensionPoint == null) {
            return;
        }
        IConfigurationElement[] members = extensionPoint
                .getConfigurationElements();
        // For each configuration element
        for (int m = 0; m < members.length; m++) {
            debug("setupCommExtensionPoint:" + members[m].getName());
            IConfigurationElement member = members[m];
            // Get the label of the extender plugin and the ID of the extension.
            IExtension extension = member.getDeclaringExtension();
            Object exten = null;
            String name = null;
            try {
                // The only required attribute is "instantiatorClass"
                exten = member
                        .createExecutableExtension(COMM_FACTORY_EPOINT_CLASS_ATTRIBUTE);
                // Verify that object implements
                // ISynchAsynchConnectionInstantiator
                ClassLoader cl = exten.getClass().getClassLoader();
                String clazz = exten.getClass().getName();
                // Get name and get version, if available
                name = member.getAttribute(COMM_FACTORY_EPOINT_NAME_ATTRIBUTE);
                if (name == null) {
                    name = clazz;
                }
                String description = member
                        .getAttribute(COMM_FACTORY_EPOINT_DESC_ATTRIBUTE);
                if (description == null) {
                    description = "";
                }
                // Get any arguments
                // Get any arguments
                DefaultArgs defaults = getDefaultArgs(member
                        .getChildren(ARG_ELEMENT_NAME));
                ConnectionDescription cd = new ConnectionDescription(name,
                        (ISynchAsynchConnectionInstantiator) exten,
                        description, defaults.getTypes(), defaults
                                .getDefaults(), defaults.getNames());
                debug("setupCommExtensionPoint:created description:" + cd);
                if (ConnectionFactory.containsDescription(cd)) {
                    // It's already there...log and throw as we can't use the
                    // same named factory
                    IStatus s = new Status(
                            Status.ERROR,
                            bundleName,
                            FACTORY_NAME_COLLISION_ERRORCODE,
                            getResourceString("ExtPointError.CommNameCollisionPrefix")
                                    + name
                                    + getResourceString("ExtPointError.CommNameCollisionSuffix")
                                    + extension
                                            .getExtensionPointUniqueIdentifier(),
                            null);
                    throw new CoreException(getStatusForCommException(
                            extension, bundleName, name));
                }
                // Now add the description and we're ready to go.
                ConnectionFactory.addDescription(cd);
                debug("setupCommExtensionPoint:added description to factory:"
                        + cd);
            } catch (CoreException e) {
                log(e.getStatus());
                dumpStack("CoreException in setupCommExtensionPoint", e);
            } catch (Exception e) {
                log(getStatusForCommException(extension, bundleName, name));
                dumpStack("Exception in setupCommExtensionPoint", e);
            }
        }
    }

    protected IStatus getStatusForCommException(IExtension ext,
            String bundleName, String name) {
        IStatus s = new Status(
                Status.ERROR,
                bundleName,
                FACTORY_NAME_COLLISION_ERRORCODE,
                getResourceString("ExtPointError.CommNameCollisionPrefix")
                        + name
                        + getResourceString("ExtPointError.CommNameCollisionSuffix")
                        + ext.getExtensionPointUniqueIdentifier(), null);
        return s;
    }

    protected IStatus getStatusForContException(IExtension ext,
            String bundleName, String name) {
        IStatus s = new Status(
                Status.ERROR,
                bundleName,
                FACTORY_NAME_COLLISION_ERRORCODE,
                getResourceString("ExtPointError.ContainerNameCollisionPrefix")
                        + name
                        + getResourceString("ExtPointError.ContainerNameCollisionSuffix")
                        + ext.getExtensionPointUniqueIdentifier(), null);
        return s;
    }

    protected IStatus getStatusForIDException(IExtension ext,
            String bundleName, String name) {
        IStatus s = new Status(
                Status.ERROR,
                bundleName,
                FACTORY_NAME_COLLISION_ERRORCODE,
                getResourceString("ExtPointError.CommNameCollisionPrefix")
                        + name
                        + getResourceString("ExtPointError.CommNameCollisionSuffix")
                        + ext.getExtensionPointUniqueIdentifier(), null);
        return s;
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        setupContainerExtensionPoint(context);
        setupIdentityExtensionPoint(context);
        setupCommExtensionPoint(context);
        this.context = context;
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        this.context = null;
    }

    public BundleContext getBundleContext() {
        return context;
    }

    /**
     * Returns the shared instance.
     */
    public static ECFPlugin getDefault() {
        return plugin;
    }

    /**
     * Returns the string from the plugin's resource bundle, or 'key' if not
     * found.
     */
    public static String getResourceString(String key) {
        ResourceBundle bundle = ECFPlugin.getDefault().getResourceBundle();
        try {
            return (bundle != null) ? bundle.getString(key) : "!" + key + "!";
        } catch (MissingResourceException e) {
            return "!" + key + "!";
        }
    }

    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }
}