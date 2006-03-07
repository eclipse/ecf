/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc., Peter Nehrer, Boris Bokowski. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.datashare;

import org.eclipse.ecf.core.ISharedObjectTransactionConfig;
import org.eclipse.ecf.core.SharedObjectDescription;

public class BasicChannelConfig implements IChannelConfig {
	
	protected SharedObjectDescription description = null;
	protected IChannelListener listener = null;
	
	public BasicChannelConfig() {}
	
	public BasicChannelConfig(SharedObjectDescription description) {
		this.description = description;
	}
	public BasicChannelConfig(SharedObjectDescription description, IChannelListener listener) {
		this.description = description;
		this.listener = listener;
	}
	public SharedObjectDescription getPrimaryDescription() {
		return description;
	}
	public IChannelListener getListener() {
		return listener;
	}
	public ISharedObjectTransactionConfig getTransactionConfig() {
		return null;
	}
	public Object getAdapter(Class adapter) {
		return null;
	}
}
