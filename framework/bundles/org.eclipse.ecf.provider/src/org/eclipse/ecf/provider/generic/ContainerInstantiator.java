package org.eclipse.ecf.provider.generic;

import org.eclipse.ecf.core.ISharedObjectContainer;
import org.eclipse.ecf.core.SharedObjectContainerInstantiationException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.provider.ISharedObjectContainerInstantiator;

public class ContainerInstantiator implements ISharedObjectContainerInstantiator {

    public ContainerInstantiator() {
        super();
    }

    public ISharedObjectContainer makeInstance(Class[] argTypes, Object[] args)
            throws SharedObjectContainerInstantiationException {
        try {
            Boolean isClient = new Boolean(true);
            ID id = null;
            if (args != null) {
	            if (args.length == 2) {
	            	isClient = (Boolean) args[0];
	            	id = (ID) args[1];
	            } else if (args.length == 1) {
	            	id = (ID) args[0];
	            }
            } else {
            	id = IDFactory.makeGUID();
            }
            ISharedObjectContainer result = null;
            if (isClient.booleanValue()) {
                return new TCPClientSOContainer(new SOContainerConfig(id));
            } else {
                return new TCPServerSOContainer(new SOContainerConfig(id));
            }
        } catch (Exception e) {
            throw new SharedObjectContainerInstantiationException("Exception creating generic container",e);
        }
    }

}
