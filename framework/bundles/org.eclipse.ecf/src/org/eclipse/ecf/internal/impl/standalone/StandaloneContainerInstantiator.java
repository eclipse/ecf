/*
 * Created on Dec 6, 2004
 *
 */
package org.eclipse.ecf.internal.impl.standalone;

import org.eclipse.ecf.core.ISharedObjectContainer;
import org.eclipse.ecf.core.SharedObjectContainerInstantiationException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.IDInstantiationException;
import org.eclipse.ecf.core.provider.ISharedObjectContainerInstantiator;
import org.eclipse.ecf.internal.core.ECFPlugin;

public class StandaloneContainerInstantiator implements
        ISharedObjectContainerInstantiator {

    public StandaloneContainerInstantiator() {
        super();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.provider.ISharedObjectContainerInstantiator#makeInstance(java.lang.Class[], java.lang.Object[])
     */
    public ISharedObjectContainer makeInstance(Class[] argTypes, Object[] args)
            throws SharedObjectContainerInstantiationException {
        ID newID = null;
        if (args == null || args.length == 0) {
            try {
                newID = IDFactory.makeGUID();
            } catch (IDInstantiationException e) {
                throw new SharedObjectContainerInstantiationException(
                        "Cannot create GUID ID for StandaloneContainer");
            }
        } else {
            try {
                newID = IDFactory.makeStringID((String) args[0]);
            } catch (IDInstantiationException e) {
                throw new SharedObjectContainerInstantiationException(
                        "Cannot create GUID ID for StandaloneContainer");
            }
        }
        return new StandaloneContainer(new StandaloneConfig(newID),ECFPlugin.getDefault().getBundleContext());
    }

}
