/****************************************************************************
 * Copyright (c) 2018 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.osgi.services.remoteserviceadmin.console;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.service.command.CommandSession;
import org.apache.felix.service.command.Converter;
import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.Parameter;
import org.eclipse.ecf.console.AbstractCommand;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IIDFactory;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.DebugRemoteServiceAdminListener;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescriptionReader;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescriptionWriter;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.remoteserviceadmin.RemoteConstants;

@Component(immediate = true, property = { "osgi.command.scope=ecf", "osgi.command.function=listexports",
		"osgi.command.function=lexps", "osgi.command.function=listimports", "osgi.command.function=limps",
		"osgi.command.function=unexportservice", "osgi.command.function=unexpsvc",
		"osgi.command.function=unimportservice", "osgi.command.function=unimpsvc", "osgi.command.function=rsadebug",
		"osgi.command.function=exportservice", "osgi.command.function=expsvc", "osgi.command.function=importservice",
		"osgi.command.function=impsvc", "osgi.command.function=updateservice",
		"osgi.command.function=updsvc" }, service = { RSACommand.class, Converter.class })
public class RSACommand extends AbstractCommand implements Converter {

	private static final String DEFAULT_EXPORT_CONFIG = System.getProperty(
			"org.eclipse.ecf.osgi.services.remoteserviceadmin.console.defaultconfig", "ecf.generic.server");

	private static final boolean DEBUGON = Boolean.parseBoolean(
			System.getProperty("org.eclipse.ecf.osgi.services.remoteserviceadmin.console.rsadebug", "true"));

	private static final String EXPORT_LINE_FORMAT = System.getProperty(
			"org.eclipse.ecf.osgi.services.remoteserviceadmin.console.exportlineformat", "%1$-37s|%2$-45s|%3$s");
	private static final String IMPORT_LINE_FORMAT = System.getProperty(
			"org.eclipse.ecf.osgi.services.remoteserviceadmin.console.importlineformat", "%1$-37s|%2$-45s|%3$s");

	private IContainerManager containerManager;
	private IIDFactory idFactory;
	private RemoteServiceAdmin rsa;
	private BundleContext context;
	private ServiceRegistration<?> debugReg;

	@Reference
	void bindContainerManager(IContainerManager cm) {
		this.containerManager = cm;
	}

	void unbindContainerManager(IContainerManager cm) {
		this.containerManager = null;
	}

	@Reference
	void bindIDFactory(IIDFactory idf) {
		this.idFactory = idf;
	}

	void unbindIDFactory(IIDFactory idf) {
		this.idFactory = null;
	}

	@Reference
	void bindRSA(org.osgi.service.remoteserviceadmin.RemoteServiceAdmin rsa) {
		this.rsa = (RemoteServiceAdmin) rsa;
	}

	void unbindRSA(org.osgi.service.remoteserviceadmin.RemoteServiceAdmin rsa) {
		this.rsa = null;
	}

	@Override
	protected IContainerManager getContainerManager() {
		return this.containerManager;
	}

	@Override
	protected IIDFactory getIDFactory() {
		return this.idFactory;
	}

	private RemoteServiceAdmin getRSA() {
		return this.rsa;
	}

	private List<RemoteServiceAdmin.ExportReference> getExports() {
		List<RemoteServiceAdmin.ExportReference> results = new ArrayList<RemoteServiceAdmin.ExportReference>();
		for (org.osgi.service.remoteserviceadmin.ExportReference er : getRSA().getExportedServices())
			results.add((RemoteServiceAdmin.ExportReference) er);
		return results;
	}

	private List<RemoteServiceAdmin.ImportReference> getImports() {
		List<RemoteServiceAdmin.ImportReference> results = new ArrayList<RemoteServiceAdmin.ImportReference>();
		for (org.osgi.service.remoteserviceadmin.ImportReference er : getRSA().getImportedEndpoints())
			results.add((RemoteServiceAdmin.ImportReference) er);
		return results;
	}

	public Object convert(Class<?> desiredType, Object in) throws Exception {
		if (desiredType == RemoteServiceAdmin.ExportReference.class && in instanceof String)
			return getExportReferenceForIdOrContainerId((String) in);
		else if (desiredType == RemoteServiceAdmin.ExportReference.class && in instanceof Long)
			return getExportReferenceForServiceId((Long) in);
		else if (desiredType == RemoteServiceAdmin.ImportReference.class && in instanceof String)
			return getImportReferenceForIdOrContainerId((String) in);
		else if (desiredType == RemoteServiceAdmin.ExportReference.class && in instanceof Long)
			return getImportReferenceForServiceId((Long) in);

		return null;
	}

	private String formatExportReference(RemoteServiceAdmin.ExportReference er, int level, Converter escape) {
		EndpointDescription ed = (EndpointDescription) er.getExportedEndpoint();
		if (ed == null)
			return null;
		return formatReference(EXPORT_LINE_FORMAT, ed, ed.getContainerID().getName(), ed.getServiceId(), level, escape);
	}

	private String formatReference(String lineFormat, EndpointDescription ed, String containerId, long serviceId,
			int level, Converter escape) {
		if (ed == null)
			return null;
		switch (level) {
		case Converter.PART:
			return null;
		case Converter.LINE:
			return formatLine(lineFormat, ed.getId(), containerId, serviceId);
		case Converter.INSPECT:
			return formatEndpoint(ed);
		default:
			return null;
		}
	}

	private String formatImportReference(RemoteServiceAdmin.ImportReference ir, int level, Converter escape) {
		ID localContainerID = ir.getLocalContainerID();
		if (localContainerID == null)
			return null;
		@SuppressWarnings("rawtypes")
		ServiceReference ref = ir.getImportedService();
		if (ref == null)
			return null;
		return formatReference(IMPORT_LINE_FORMAT, (EndpointDescription) ir.getImportedEndpoint(),
				localContainerID.getName(), (Long) ref.getProperty(Constants.SERVICE_ID), level, escape);
	}

	private String formatEndpoint(EndpointDescription ed) {
		EndpointDescriptionWriter edw = new EndpointDescriptionWriter();
		StringWriter sw = new StringWriter();
		try {
			edw.writeEndpointDescription(sw, ed);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return formatLine("%s", sw.toString());
	}

	public String format(Object target, int level, Converter escape) {
		if (target instanceof RemoteServiceAdmin.ExportReference)
			return formatExportReference((RemoteServiceAdmin.ExportReference) target, level, escape);
		else if (target instanceof RemoteServiceAdmin.ImportReference)
			return formatImportReference((RemoteServiceAdmin.ImportReference) target, level, escape);
		return null;
	}

	@Activate
	void activate(BundleContext context) {
		this.context = context;
		if (DEBUGON)
			debugOn();
	}

	@Deactivate
	void deactivate() {
		debugOff();
	}

	synchronized void debugOff() {
		if (debugReg != null) {
			debugReg.unregister();
			debugReg = null;
		}
	}

	synchronized void debugOn() {
		if (debugReg == null)
			debugReg = this.context.registerService(
					org.osgi.service.remoteserviceadmin.RemoteServiceAdminListener.class,
					new DebugRemoteServiceAdminListener(), null);
	}

	private RemoteServiceAdmin.ExportReference getExportReferenceForIdOrContainerId(String exportRefId) {
		for (RemoteServiceAdmin.ExportReference r : getExports()) {
			EndpointDescription ed = (EndpointDescription) r.getExportedEndpoint();
			if (ed != null && (ed.getId().equals(exportRefId) || ed.getContainerID().getName().equals(exportRefId)))
				return r;
		}
		return null;
	}

	private RemoteServiceAdmin.ExportRegistration getExportRegistrationForId(String id) {
		for (RemoteServiceAdmin.ExportRegistration r : getRSA().getExportedRegistrations()) {
			RemoteServiceAdmin.ExportReference er = (RemoteServiceAdmin.ExportReference) r.getExportReference();
			if (er != null) {
				EndpointDescription ed = (EndpointDescription) er.getExportedEndpoint();
				if (ed != null && ed.getId().equals(id))
					return r;
			}
		}
		return null;
	}

	private RemoteServiceAdmin.ImportRegistration getImportRegistrationForId(String id) {
		for (RemoteServiceAdmin.ImportRegistration r : getRSA().getImportedRegistrations()) {
			RemoteServiceAdmin.ImportReference er = (RemoteServiceAdmin.ImportReference) r.getImportReference();
			if (er != null) {
				EndpointDescription ed = (EndpointDescription) er.getImportedEndpoint();
				if (ed != null && ed.getId().equals(id))
					return r;
			}
		}
		return null;
	}

	private RemoteServiceAdmin.ExportReference getExportReferenceForServiceId(Long serviceId) {
		for (RemoteServiceAdmin.ExportReference r : getExports()) {
			EndpointDescription ed = (EndpointDescription) r.getExportedEndpoint();
			if (ed != null && ed.getServiceId() == (long) serviceId)
				return r;
		}
		return null;
	}

	private RemoteServiceAdmin.ImportReference getImportReferenceForServiceId(Long serviceId) {
		for (RemoteServiceAdmin.ImportReference r : getImports()) {
			EndpointDescription ed = (EndpointDescription) r.getImportedEndpoint();
			if (ed != null && ed.getServiceId() == (long) serviceId)
				return r;
		}
		return null;
	}

	private RemoteServiceAdmin.ImportReference getImportReferenceForIdOrContainerId(String importRefId) {
		for (RemoteServiceAdmin.ImportReference r : getImports()) {
			EndpointDescription ed = (EndpointDescription) r.getImportedEndpoint();
			if (ed != null && (ed.getId().equals(importRefId) || ed.getContainerID().getName().equals(importRefId)))
				return r;
		}
		return null;
	}

	@Descriptor("List RSA exported services")
	public List<RemoteServiceAdmin.ExportReference> listexports(CommandSession cs) {
		consoleLine(cs, EXPORT_LINE_FORMAT, "endpoint.id", "Exporting Container ID", "Exported Service Id\n");
		return getExports();
	}

	@Descriptor("List RSA exported services")
	public List<RemoteServiceAdmin.ExportReference> lexps(CommandSession cs) {
		return listexports(cs);
	}

	@Descriptor("Details about a single RSA exported service")
	public RemoteServiceAdmin.ExportReference listexports(
			@Descriptor("The endpoint.id of the exported service") RemoteServiceAdmin.ExportReference r) {
		return r;
	}

	@Descriptor("Details about a single RSA exported service")
	public RemoteServiceAdmin.ExportReference lexps(
			@Descriptor("The endpoint.id of the exported service") RemoteServiceAdmin.ExportReference r) {
		return r;
	}

	@Descriptor("List RSA imported services")
	public List<RemoteServiceAdmin.ImportReference> listimports(CommandSession cs) {
		consoleLine(cs, IMPORT_LINE_FORMAT, "endpoint.id", "Importing Container ID", "Imported Service Id\n");
		return getImports();
	}

	@Descriptor("List RSA imported services")
	public List<RemoteServiceAdmin.ImportReference> limps(CommandSession cs) {
		return listimports(cs);
	}

	@Descriptor("Details about a single RSA imported service")
	public RemoteServiceAdmin.ImportReference listimports(
			@Descriptor("The endpoint.id of the exported service") RemoteServiceAdmin.ImportReference r) {
		return r;
	}

	@Descriptor("Details about a single RSA imported service")
	public RemoteServiceAdmin.ImportReference limps(
			@Descriptor("The endpoint.id of the exported service") RemoteServiceAdmin.ImportReference r) {
		return r;
	}

	@Descriptor("Unexport an RSA exported service")
	public String unexportservice(@Descriptor("The endpoint.id of the exported service") String endpointId) {
		RemoteServiceAdmin.ExportRegistration reg = getExportRegistrationForId(endpointId);
		if (reg != null) {
			reg.close();
			return endpointId + " unexported";
		}
		return endpointId + " not found";
	}

	@Descriptor("Unexport an RSA exported service")
	public String unexpsvc(@Descriptor("The endpoint.id of the exported service") String endpointId) {
		return unexportservice(endpointId);
	}

	@Descriptor("Unimport an RSA imported service")
	public String unimportservice(@Descriptor("The endpoint.id of the imported service") String endpointId) {
		RemoteServiceAdmin.ImportRegistration reg = getImportRegistrationForId(endpointId);
		if (reg != null) {
			reg.close();
			return endpointId + " unimported";
		}
		return endpointId + " not found";
	}

	@Descriptor("Unimport an RSA imported service")
	public String unimpsvc(@Descriptor("The endpoint.id of the imported service") String endpointId) {
		return unimportservice(endpointId);
	}

	@Descriptor("Toggle whether RSA debug output is output to console")
	public String rsadebug() {
		synchronized (this) {
			return rsadebug(debugReg == null);
		}
	}

	@Descriptor("Set whether RSA debug output is output to console")
	public String rsadebug(@Descriptor("Whether to turn debug on or off") boolean on) {
		synchronized (this) {
			if (debugReg == null) {
				if (on) {
					debugOn();
					return "RSA debugging ON";
				} else
					return "RSA debugging already off";
			} else {
				if (on)
					return "RSA debugging already on";
				else {
					debugOff();
					return "RSA debugging OFF";
				}
			}
		}
	}

	@Descriptor("Export a service via Remote Service Admin")
	public RemoteServiceAdmin.ExportReference exportservice(CommandSession cs,
			@Descriptor("service.id of service to export") long serviceid,
			@Descriptor("Map of service properties for exporting the service") Map<String, ?> map) {
		ServiceReference<?> ref = null;
		try {
			ServiceReference<?>[] refs = context.getAllServiceReferences(null,
					"(" + Constants.SERVICE_ID + "=" + String.valueOf(serviceid) + ")");
			if (refs == null || refs.length < 1)
				cs.getConsole().println("Cannot find registered service with service.id=" + String.valueOf(serviceid));
			ref = refs[0];
		} catch (InvalidSyntaxException e) {
			e.printStackTrace(cs.getConsole());
			return null;
		}
		// Create map given map from console
		Map<String, Object> op = (map == null) ? new HashMap<String, Object>() : new HashMap<String, Object>(map);
		if (!op.containsKey(RemoteConstants.SERVICE_EXPORTED_INTERFACES))
			op.put(RemoteConstants.SERVICE_EXPORTED_INTERFACES, "*");
		if (!op.containsKey(RemoteConstants.SERVICE_EXPORTED_CONFIGS))
			op.put(RemoteConstants.SERVICE_EXPORTED_CONFIGS, DEFAULT_EXPORT_CONFIG);
		// Now export service with reference and overriding properties
		Collection<org.osgi.service.remoteserviceadmin.ExportRegistration> regs = getRSA().exportService(ref, op);
		// Should always return >= 1 registration
		if (regs != null)
			for (org.osgi.service.remoteserviceadmin.ExportRegistration reg : regs) {
				Throwable t = reg.getException();
				if (t != null)
					t.printStackTrace(cs.getConsole());
				else {
					RemoteServiceAdmin.ExportReference er = (RemoteServiceAdmin.ExportReference) reg
							.getExportReference();
					if (er != null) {
						cs.getConsole().println("service.id=" + String.valueOf(serviceid)
								+ " successfully exported with endpoint description:");
						return er;
					}
				}
			}
		return null;
	}

	@Descriptor("Export a service via Remote Service Admin")
	public RemoteServiceAdmin.ExportReference exportservice(CommandSession cs,
			@Descriptor("service.id of service to export") long serviceid) {
		return exportservice(cs, serviceid, null);
	}

	@Descriptor("Export a service via Remote Service Admin")
	public RemoteServiceAdmin.ExportReference expsvc(CommandSession cs,
			@Descriptor("service.id of service to export") long serviceid,
			@Descriptor("Map of service properties for exporting the service") Map<String, ?> map) {
		return exportservice(cs, serviceid, map);
	}

	@Descriptor("Export a service via Remote Service Admin")
	public RemoteServiceAdmin.ExportReference expsvc(CommandSession cs,
			@Descriptor("service.id of service to export") long serviceid) {
		return exportservice(cs, serviceid, null);
	}

	@Descriptor("Import a remote service via Remote Service Admin.  If -e is used, the given endpoint URL is read to read the EndpointDescription.  If not used, an EndpointDescription is expected from the console input (e.g. copy and paste)")
	public RemoteServiceAdmin.ImportReference importservice(CommandSession cs,
			@Descriptor("Optional URL indicating location of an Endpoint Description (EDEF format)") @Parameter(names = {
					"-e", "--edefurl" }, absentValue = "") String endpointurl) {
		InputStream ins = null;
		URL url = null;
		if ("".equals(endpointurl)) {
			ins = cs.getKeyboard();
			cs.getConsole().println("Waiting for console input.   To complete enter an empty line...");
		} else {
			try {
				url = new URL(endpointurl);
				ins = url.openStream();
			} catch (IOException e) {
				e.printStackTrace(cs.getConsole());
			}
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(ins));
		StringBuffer buf = new StringBuffer();
		// read from the input stream until an empty line is encountered
		while (true) {
			try {
				String line = br.readLine();
				if (line != null && line.length() > 0)
					buf.append(line).append("\n");
				else
					break;
			} catch (IOException e) {
				e.printStackTrace(cs.getConsole());
				return null;
			}
		}
		// Close the input stream if this was from a url
		if (url != null)
			try {
				ins.close();
			} catch (IOException e) {
				e.printStackTrace(cs.getConsole());
			}

		ByteArrayInputStream bins = new ByteArrayInputStream(buf.toString().getBytes());
		EndpointDescriptionReader r = new EndpointDescriptionReader();
		org.osgi.service.remoteserviceadmin.EndpointDescription[] eds = null;
		try {
			eds = r.readEndpointDescriptions(bins);
		} catch (IOException e) {
			e.printStackTrace(cs.getConsole());
			return null;
		}
		// should be only one
		org.osgi.service.remoteserviceadmin.ImportRegistration reg = getRSA().importService(eds[0]);
		if (reg == null)
			return null;
		else {
			Throwable t = reg.getException();
			if (t != null) {
				t.printStackTrace(cs.getConsole());
				return null;
			} else {
				RemoteServiceAdmin.ImportReference ir = (RemoteServiceAdmin.ImportReference) reg.getImportReference();
				if (ir != null) {
					EndpointDescription ed = (EndpointDescription) ir.getImportedEndpoint();
					if (ed == null) {
						cs.getConsole().println("Cannot get endpoint description for imported endpoint");
						return null;
					}
					cs.getConsole().println("endpoint.id=" + ed.getId() + " with service.id="
							+ ir.getImportedService().getProperty(Constants.SERVICE_ID) + " successfully imported:");
					return ir;
				} else
					return null;
			}
		}
	}

	@Descriptor("Import a remote service via Remote Service Admin.  If -e|-edefurl is used, the given endpoint URL is read to read the EndpointDescription.  If not used, an EndpointDescription is expected from the console input (e.g. copy and paste)")
	public RemoteServiceAdmin.ImportReference impsvc(CommandSession cs,
			@Descriptor("Optional URL indicating location of an Endpoint Description (EDEF format)") @Parameter(names = {
					"-e", "--edefurl" }, absentValue = "") String endpointurl) {
		return importservice(cs, endpointurl);
	}

	@Descriptor("Update the properties of a remote service via Remote Service Admin")
	public RemoteServiceAdmin.ExportReference updateservice(CommandSession cs,
			@Descriptor("endpoint.id of remote service to update") String endpointid,
			@Descriptor("Map of properties for update") Map<String, ?> map) {
		RemoteServiceAdmin.ExportRegistration ereg = getExportRegistrationForId(endpointid);
		if (ereg == null) {
			cs.getConsole().println("Cannot find export with endpoint.id=" + endpointid);
			return null;
		}
		RemoteServiceAdmin.ExportReference eref = (RemoteServiceAdmin.ExportReference) ereg.getExportReference();
		if (eref == null) {
			cs.getConsole().println("The remote service with endpoint.id=" + endpointid + " has been closed");
			return null;
		}
		// Do the update
		ereg.update(map);
		cs.getConsole().println("The endpoint.id=" + endpointid + " has been updated");
		return eref;
	}

	@Descriptor("Update the properties of a remote service via Remote Service Admin")
	public RemoteServiceAdmin.ExportReference updsvc(CommandSession cs,
			@Descriptor("endpoint.id of remote service to update") String endpointid,
			@Descriptor("Map of properties for update") Map<String, ?> map) {
		return updateservice(cs, endpointid, map);
	}

}
