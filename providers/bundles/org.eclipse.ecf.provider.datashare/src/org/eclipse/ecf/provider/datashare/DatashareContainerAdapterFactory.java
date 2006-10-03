package org.eclipse.ecf.provider.datashare;

import org.eclipse.ecf.core.AbstractSharedObjectContainerAdapterFactory;
import org.eclipse.ecf.core.ISharedObject;
import org.eclipse.ecf.core.ISharedObjectContainer;
import org.eclipse.ecf.datashare.IChannelContainer;

public class DatashareContainerAdapterFactory extends
		AbstractSharedObjectContainerAdapterFactory {

	protected ISharedObject createAdapter(ISharedObjectContainer container, Class adapterType) {
		if (adapterType.equals(IChannelContainer.class)) {
			return new SharedObjectDatashareContainerAdapter();
		} else return null;
	}

	public Class[] getAdapterList() {
		return new Class[] { IChannelContainer.class };
	}

}
