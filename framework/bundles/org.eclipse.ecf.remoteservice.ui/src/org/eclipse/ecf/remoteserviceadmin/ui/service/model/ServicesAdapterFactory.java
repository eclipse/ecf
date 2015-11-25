/*******************************************************************************
 * Copyright (c) 2015 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Scott Lewis - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.remoteserviceadmin.ui.service.model;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;
import org.eclipse.ui.model.IWorkbenchAdapter3;

/**
 * @since 3.3
 */
public class ServicesAdapterFactory implements IAdapterFactory {

	private ServicesRootNodeWorkbenchAdapter serviceRootAdapter = new ServicesRootNodeWorkbenchAdapter();
	private ServiceNodeWorkbenchAdapter serviceAdapter = new ServiceNodeWorkbenchAdapter();
	private BundleIdNodeWorkbenchAdapter bundleIdAdapter = new BundleIdNodeWorkbenchAdapter();
	private RegisteringBundleIdNodeWorkbenchAdapter registeringBundleIdAdapter = new RegisteringBundleIdNodeWorkbenchAdapter();
	private UsingBundleIdsNodeWorkbenchAdapter usingBundleIdsAdapter = new UsingBundleIdsNodeWorkbenchAdapter();

	@Override
	public Object getAdapter(Object adaptableObject, @SuppressWarnings("rawtypes") Class adapterType) {
		if (adapterType.isInstance(adaptableObject)) {
			return adaptableObject;
		}
		if (adapterType == IWorkbenchAdapter.class || adapterType == IWorkbenchAdapter2.class
				|| adapterType == IWorkbenchAdapter3.class) {
			return getWorkbenchElement(adaptableObject);
		}
		return null;
	}

	protected Object getWorkbenchElement(Object adaptableObject) {
		if (adaptableObject instanceof ServicesRootNode)
			return serviceRootAdapter;
		if (adaptableObject instanceof ServiceNode)
			return serviceAdapter;
		if (adaptableObject instanceof RegisteringBundleIdNode)
			return registeringBundleIdAdapter;
		if (adaptableObject instanceof UsingBundleIdsNode)
			return usingBundleIdsAdapter;
		if (adaptableObject instanceof BundleIdNode)
			return bundleIdAdapter;
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class[] getAdapterList() {
		return new Class[] { IWorkbenchAdapter.class, IWorkbenchAdapter2.class, IWorkbenchAdapter3.class };
	}

}
