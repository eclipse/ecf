package org.eclipse.ecf.provider.generic;

import org.eclipse.ecf.core.ISharedObjectContainer;
import org.eclipse.ecf.core.SharedObjectContainerInstantiationException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.provider.ISharedObjectContainerInstantiator;

public class ContainerInstantiator implements ISharedObjectContainerInstantiator {

    public ContainerInstantiator() {
        super();
    }

    public ISharedObjectContainer makeInstance(Class[] argTypes, Object[] args)
            throws SharedObjectContainerInstantiationException {
        try {
            Boolean isClient = null;
            ID id = null;
            isClient = (Boolean) args[0];
            id = (ID) args[1];
            ISharedObjectContainer result = null;
            if (isClient.booleanValue()) {
                return new TCPClientSOContainer(new SOContainerConfig(id));
            } else {
                return new TCPServerSOContainer(new SOContainerConfig(id));
            }
        } catch (Exception e) {
            throw new SharedObjectContainerInstantiationException("Exception creating container",e);
        }
    }

}
