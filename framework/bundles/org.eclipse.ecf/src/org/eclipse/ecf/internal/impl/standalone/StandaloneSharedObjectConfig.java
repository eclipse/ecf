/*
 * Created on Nov 29, 2004
 *
 */
package org.eclipse.ecf.internal.impl.standalone;

import java.util.Map;

import org.eclipse.ecf.core.ISharedObjectConfig;
import org.eclipse.ecf.core.ISharedObjectContext;
import org.eclipse.ecf.core.identity.ID;

public class StandaloneSharedObjectConfig implements ISharedObjectConfig {

    StandaloneContainer container = null;
    ID sharedObjectID;
    ID homeContainerID;
    boolean isActive;
    Map properties;
    
    public StandaloneSharedObjectConfig(ID sharedObjectID, ID homeContainerID, StandaloneContainer cont, Map dict) {
        super();
        this.sharedObjectID = sharedObjectID;
        this.homeContainerID = homeContainerID;
        this.container = cont;
        isActive = false;
        properties = dict;
    }
    
    protected void makeActive() {
        isActive = true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.ISharedObjectConfig#getSharedObjectID()
     */
    public ID getSharedObjectID() {
        return sharedObjectID;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.ISharedObjectConfig#getHomeContainerID()
     */
    public ID getHomeContainerID() {
        return homeContainerID;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.ISharedObjectConfig#getContext()
     */
    public ISharedObjectContext getContext() {
        if (isActive) {
            return null;
        } else return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.ISharedObjectConfig#getProperties()
     */
    public Map getProperties() {
        return properties;
    }

}
