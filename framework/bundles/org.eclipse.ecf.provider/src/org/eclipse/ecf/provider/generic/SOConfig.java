/*
 * Created on Nov 29, 2004
 *  
 */
package org.eclipse.ecf.provider.generic;

import java.util.Map;

import org.eclipse.ecf.core.ISharedObjectConfig;
import org.eclipse.ecf.core.ISharedObjectContext;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.QueueEnqueue;

public class SOConfig implements ISharedObjectConfig {

    SOContainer container = null;
    ID sharedObjectID;
    ID homeContainerID;
    boolean isActive;
    Map properties;
    SOContext standAloneContext;

    public SOConfig(ID sharedObjectID, ID homeContainerID, SOContainer cont,
            Map dict) {
        super();
        this.sharedObjectID = sharedObjectID;
        this.homeContainerID = homeContainerID;
        isActive = false;
        properties = dict;
        this.container = cont;
    }

    protected void makeActive(QueueEnqueue queue) {
        isActive = true;
        this.standAloneContext = new SOContext(sharedObjectID, homeContainerID,
                container, properties, queue);
    }

    protected void makeInactive() {
        this.standAloneContext.makeInactive();
        this.standAloneContext = null;
        isActive = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObjectConfig#getSharedObjectID()
     */
    public ID getSharedObjectID() {
        return sharedObjectID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObjectConfig#getHomeContainerID()
     */
    public ID getHomeContainerID() {
        return homeContainerID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObjectConfig#getContext()
     */
    public ISharedObjectContext getContext() {
        if (isActive) {
            return null;
        } else
            return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObjectConfig#getProperties()
     */
    public Map getProperties() {
        return properties;
    }

}