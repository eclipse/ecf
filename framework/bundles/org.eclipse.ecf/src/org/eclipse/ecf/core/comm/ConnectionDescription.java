package org.eclipse.ecf.core.comm;

import org.eclipse.ecf.core.comm.provider.ISynchAsynchConnectionInstantiator;

public class ConnectionDescription {

    protected String name;
    protected String instantiatorClass;

    protected ISynchAsynchConnectionInstantiator instantiator;
    protected int hashCode = 0;
    protected ClassLoader classLoader = null;
    protected String description;
    
    public ConnectionDescription(ClassLoader loader,

    String name, String instantiatorClass, String desc) {
        if (name == null)
            throw new RuntimeException(new InstantiationException(
                    "stagecontainer description name cannot be null"));
        this.classLoader = loader;
        this.name = name;
        this.instantiatorClass = instantiatorClass;
        this.hashCode = name.hashCode();
    }
    public ConnectionDescription(String name, ISynchAsynchConnectionInstantiator inst, String desc) {
        this.instantiator = inst;
        this.classLoader = this.instantiator.getClass().getClassLoader();
        this.instantiatorClass = this.instantiatorClass.getClass().getName();
        this.hashCode = name.hashCode();
        this.description = desc;
    }
    public String getName() {
        return name;
    }
    public ClassLoader getClassLoader() {
        return classLoader;
    }
    public boolean equals(Object other) {
        if (!(other instanceof ConnectionDescription))
            return false;
        ConnectionDescription scd = (ConnectionDescription) other;
        return scd.name.equals(name);
    }

    public int hashCode() {
        return hashCode;
    }

    public String toString() {
        StringBuffer b = new StringBuffer("ConnectionDescription[");
        b.append(name).append(";");
        b.append(instantiatorClass).append(";").append(description).append("]");
        return b.toString();
    }

    protected ISynchAsynchConnectionInstantiator getInstantiator()
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        synchronized (this) {
            if (instantiator == null)
                initializeInstantiator(classLoader);
            return instantiator;
        }
    }

    protected void initializeInstantiator(ClassLoader cl)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        if (cl == null)
            cl = this.getClass().getClassLoader();
        // Load instantiator class
        Class clazz = Class.forName(instantiatorClass, true, cl);
        // Make new instance
        instantiator = (ISynchAsynchConnectionInstantiator) clazz.newInstance();
    }

    public String getDescription() {
        return description;
    }
    
}