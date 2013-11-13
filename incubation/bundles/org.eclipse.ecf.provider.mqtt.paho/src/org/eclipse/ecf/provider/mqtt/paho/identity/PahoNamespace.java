package org.eclipse.ecf.provider.mqtt.paho.identity;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.Namespace;

public class PahoNamespace extends Namespace {

	public static final String NAME = "org.eclipse.provider.paho.namespace";
	public static final String SCHEME = "paho";
	
	static Namespace INSTANCE;
	
	private static final long serialVersionUID = -1856480656826761786L;

	public PahoNamespace() {
		INSTANCE = this;
	}

	public PahoNamespace(String name, String desc) {
		super(name, desc);
		INSTANCE = this;
	}

	@Override
	public ID createInstance(Object[] parameters) throws IDCreateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getScheme() {
		return SCHEME;
	}

}
