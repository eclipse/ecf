package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.DebugOptions;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.LogUtility;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin.ExportReference;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin.ImportReference;
import org.osgi.framework.ServiceReference;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminEvent;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminListener;

/**
 * @since 4.3
 */
public class DebugRemoteServiceAdminListener implements
		RemoteServiceAdminListener {

	public static final SimpleDateFormat sdf = new SimpleDateFormat(
			"HH:mm:ss.SSS"); //$NON-NLS-1$

	public static final int EXPORT_MASK = RemoteServiceAdminEvent.EXPORT_ERROR
			| RemoteServiceAdminEvent.EXPORT_REGISTRATION
			| RemoteServiceAdminEvent.EXPORT_UNREGISTRATION
			| RemoteServiceAdminEvent.EXPORT_WARNING;
	public static final int IMPORT_MASK = RemoteServiceAdminEvent.IMPORT_ERROR
			| RemoteServiceAdminEvent.IMPORT_REGISTRATION
			| RemoteServiceAdminEvent.IMPORT_UNREGISTRATION
			| RemoteServiceAdminEvent.IMPORT_WARNING;

	public static final int ALL_MASK = EXPORT_MASK | IMPORT_MASK;

	protected final PrintWriter writer;
	// default is all events
	protected int eventMask = ALL_MASK;
	protected boolean writeEndpoint;
	protected EndpointDescriptionWriter edWriter;

	public DebugRemoteServiceAdminListener(PrintWriter writer, int eventMask,
			boolean writeEndpoint) {
		Assert.isNotNull(writer);
		this.writer = writer;
		this.eventMask = eventMask;
		this.writeEndpoint = writeEndpoint;
		if (this.writeEndpoint)
			edWriter = new EndpointDescriptionWriter();
	}

	public DebugRemoteServiceAdminListener(PrintWriter writer, int mask) {
		this(writer, mask, true);
	}

	public DebugRemoteServiceAdminListener(PrintWriter writer) {
		this(writer, ALL_MASK);
	}

	public DebugRemoteServiceAdminListener(int mask, boolean writeEndpoint) {
		this(new PrintWriter(System.out), mask, writeEndpoint);
	}

	public DebugRemoteServiceAdminListener(int mask) {
		this(mask, true);
	}

	public DebugRemoteServiceAdminListener() {
		this(ALL_MASK);
	}

	public int getEventMask() {
		return this.eventMask;
	}

	public void setEventMask(int eventMask) {
		this.eventMask = eventMask;
	}

	protected boolean allow(int type, int mask) {
		return (type & mask) > 0;
	}

	public void remoteAdminEvent(RemoteServiceAdminEvent event) {
		if (!(event instanceof RemoteServiceAdmin.RemoteServiceAdminEvent))
			return;
		if (allow(event.getType(), this.eventMask))
			printEvent((RemoteServiceAdmin.RemoteServiceAdminEvent) event);
	}

	protected String eventTypeToString(int type) {
		switch (type) {
		case RemoteServiceAdminEvent.EXPORT_ERROR:
			return "EXPORT_ERROR"; //$NON-NLS-1$
		case RemoteServiceAdminEvent.EXPORT_REGISTRATION:
			return "EXPORT_REGISTRATION"; //$NON-NLS-1$
		case RemoteServiceAdminEvent.EXPORT_UNREGISTRATION:
			return "EXPORT_UNREGISTRATION"; //$NON-NLS-1$
		case RemoteServiceAdminEvent.EXPORT_UPDATE:
			return "EXPORT_UPDATE"; //$NON-NLS-1$
		case RemoteServiceAdminEvent.EXPORT_WARNING:
			return "EXPORT_WARNING"; //$NON-NLS-1$
		case RemoteServiceAdminEvent.IMPORT_ERROR:
			return "IMPORT_ERROR"; //$NON-NLS-1$
		case RemoteServiceAdminEvent.IMPORT_REGISTRATION:
			return "IMPORT_REGISTRATION"; //$NON-NLS-1$
		case RemoteServiceAdminEvent.IMPORT_UNREGISTRATION:
			return "IMPORT_UNREGISTRATION"; //$NON-NLS-1$
		case RemoteServiceAdminEvent.IMPORT_UPDATE:
			return "IMPORT_UPDATE"; //$NON-NLS-1$
		case RemoteServiceAdminEvent.IMPORT_WARNING:
			return "IMPORT_WARNING"; //$NON-NLS-1$
		default:
			return "UNKNOWN"; //$NON-NLS-1$
		}
	}

	protected void writeRemoteReference(StringBuffer buf,
			ServiceReference<?> ref, ID containerID, long remoteServiceID) {
		this.writer
				.println(buf
						.append(ref)
						.append(";cID=").append(containerID).append(";rsId=").append(remoteServiceID).toString()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected void printEvent(RemoteServiceAdmin.RemoteServiceAdminEvent event) {
		ID cID = event.getContainerID();
		StringBuffer buf = new StringBuffer(sdf.format(new Date())).append(";") //$NON-NLS-1$
				.append(eventTypeToString(event.getType()));
		switch (event.getType()) {
		case RemoteServiceAdminEvent.EXPORT_REGISTRATION:
		case RemoteServiceAdminEvent.EXPORT_UNREGISTRATION:
		case RemoteServiceAdminEvent.EXPORT_UPDATE:
		case RemoteServiceAdminEvent.EXPORT_WARNING:
			ExportReference exRef = (RemoteServiceAdmin.ExportReference) event
					.getExportReference();
			if (exRef != null) {
				writeRemoteReference(
						buf.append(";exportedSR"), exRef.getExportedService(), cID, exRef.getRemoteServiceId()); //$NON-NLS-1$
				if (this.writeEndpoint)
					writeEndpoint(exRef.getEndpointDescription());
			}
			break;
		case RemoteServiceAdminEvent.IMPORT_REGISTRATION:
		case RemoteServiceAdminEvent.IMPORT_UNREGISTRATION:
		case RemoteServiceAdminEvent.IMPORT_UPDATE:
		case RemoteServiceAdminEvent.IMPORT_WARNING:
			ImportReference imRef = (RemoteServiceAdmin.ImportReference) event
					.getImportReference();
			if (imRef != null) {
				writeRemoteReference(
						buf.append(";importedSR"), imRef.getImportedService(), cID, imRef.getRemoteServiceId()); //$NON-NLS-1$
				if (this.writeEndpoint)
					writeEndpoint(imRef.getEndpointDescription());
			}
			break;
		case RemoteServiceAdminEvent.EXPORT_ERROR:
		case RemoteServiceAdminEvent.IMPORT_ERROR:
			writer.println(buf.toString());
			Throwable t = event.getException();
			if (t != null)
				t.printStackTrace(this.writer);
			break;

		}
		writer.flush();
	}

	protected void writeEndpoint(EndpointDescription endpointDescription) {
		try {
			this.writer.println("--Endpoint Description---"); //$NON-NLS-1$
			this.edWriter.writeEndpointDescription(this.writer,
					endpointDescription);
			this.writer.println("---End Endpoint Description"); //$NON-NLS-1$
		} catch (Exception e) {
			LogUtility
					.logError(
							"writeEndpoint", DebugOptions.EXCEPTIONS_CATCHING, this.getClass(), "Could not write endpoint description", e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

}
