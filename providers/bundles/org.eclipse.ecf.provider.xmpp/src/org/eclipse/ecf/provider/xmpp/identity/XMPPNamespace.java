package org.eclipse.ecf.provider.xmpp.identity;

import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.identity.provider.IDInstantiator;

public class XMPPNamespace extends Namespace {

	public XMPPNamespace(String name, IDInstantiator inst, String desc) {
		super(name, inst, desc);
	}

	private static final long serialVersionUID = 3257569499003041590L;

}
