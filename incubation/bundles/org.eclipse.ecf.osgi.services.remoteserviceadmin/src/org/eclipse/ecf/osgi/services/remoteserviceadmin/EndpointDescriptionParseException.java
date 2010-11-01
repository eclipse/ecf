package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.util.ECFException;

public class EndpointDescriptionParseException extends ECFException {

	private static final long serialVersionUID = -4481979787400184664L;

	public EndpointDescriptionParseException() {
	}

	public EndpointDescriptionParseException(String message) {
		super(message);
	}

	public EndpointDescriptionParseException(Throwable cause) {
		super(cause);
	}

	public EndpointDescriptionParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public EndpointDescriptionParseException(IStatus status) {
		super(status);
	}

}
