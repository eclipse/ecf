/*******************************************************************************
 * Copyright (c) 2014 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.internal.remoteservice.java8;

import java.util.concurrent.CompletableFuture;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.sharedobject.*;
import org.eclipse.ecf.provider.remoteservice.generic.RegistrySharedObject;
import org.eclipse.ecf.provider.remoteservice.generic.RemoteServiceContainerAdapterFactory;
import org.eclipse.ecf.provider.remoteservice.generic.RemoteServiceImpl;
import org.eclipse.ecf.provider.remoteservice.generic.RemoteServiceRegistrationImpl;
import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.events.IRemoteCallCompleteEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteCallEvent;

public class J8RemoteServiceContainerAdapterFactory extends RemoteServiceContainerAdapterFactory {

	protected ISharedObject createAdapter(ISharedObjectContainer container, @SuppressWarnings("rawtypes") Class adapterType, ID adapterID) {
		if (adapterType.equals(IRemoteServiceContainerAdapter.class)) {
			return new RegistrySharedObject() {
				@Override
				protected RemoteServiceImpl createRemoteService(
						RemoteServiceRegistrationImpl registration) {
					return new RemoteServiceImpl(this,registration) {
						@SuppressWarnings("unchecked")
						@Override
						protected Object callFuture(IRemoteCall call, @SuppressWarnings("rawtypes") Class returnType) {
							if (CompletableFuture.class.isAssignableFrom(returnType)) {
								@SuppressWarnings("rawtypes")
							    CompletableFuture result = new CompletableFuture();
								callAsyncWithResult(call, (IRemoteCallEvent e) -> { 
									if (e instanceof IRemoteCallCompleteEvent) {
										IRemoteCallCompleteEvent cce = (IRemoteCallCompleteEvent) e;
										if (cce.hadException()) 
											result.completeExceptionally(cce.getException());
										else 
											result.complete(cce.getResponse());
									}
								});
								return result;
							}
							return super.callFuture(call, returnType);
						}
					};
				}
			};
		}
		return null;
	}
}
