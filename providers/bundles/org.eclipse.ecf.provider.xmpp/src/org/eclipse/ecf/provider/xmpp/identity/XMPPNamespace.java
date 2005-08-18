package org.eclipse.ecf.provider.xmpp.identity;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDInstantiationException;
import org.eclipse.ecf.core.identity.Namespace;

public class XMPPNamespace extends Namespace {

	private static final long serialVersionUID = 3257569499003041590L;

	public ID makeInstance(Class[] argTypes, Object[] args)
			throws IDInstantiationException {
		try {
			if (args.length == 3) {
				return new XMPPID(this, (String) args[0], (String) args[1],
						(String) args[2]);
			} else if (args.length == 2) {
				return new XMPPID(this, (String) args[0], (String) args[1]);
			} else if (args.length == 1) {
				return new XMPPID(this, (String) args[0]);
			}
			throw new IllegalArgumentException(
					"XMPP ID constructor arguments invalid");
		} catch (Exception e) {
			throw new IDInstantiationException("XMPP ID creation exception", e);
		}
	}
}
