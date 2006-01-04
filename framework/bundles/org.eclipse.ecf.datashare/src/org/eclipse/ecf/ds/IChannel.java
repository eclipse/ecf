package org.eclipse.ecf.ds;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ecf.core.IIdentifiable;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;

public interface IChannel extends IAdaptable, IIdentifiable {
	public void sendMessage(byte [] message) throws ECFException;
	public void sendMessage(ID receiver, byte [] message) throws ECFException;
}
