package org.eclipse.ecf.provider.generic;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ecf.core.ISharedObjectContainerConfig;
import org.eclipse.ecf.core.identity.ID;

public class SOContainerConfig implements ISharedObjectContainerConfig {

    ID id;
    Map properties;

    public SOContainerConfig(ID id, Map props) {
        this.id = id;
        this.properties = props;
    }
    public SOContainerConfig(ID id) {
        this.id = id;
        this.properties = new HashMap();
    }
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObjectContainerConfig#getProperties()
     */
    public Map getProperties() {
        return properties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObjectContainerConfig#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class clazz) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.IIdentifiable#getID()
     */
    public ID getID() {
        return id;
    }

}