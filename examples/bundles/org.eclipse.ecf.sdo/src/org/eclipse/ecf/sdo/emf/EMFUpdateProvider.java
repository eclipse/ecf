/*******************************************************************************
 * Copyright (c) 2004 Peter Nehrer and Composent, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Peter Nehrer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.sdo.emf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.sdo.ISharedDataGraph;
import org.eclipse.ecf.sdo.IUpdateProvider;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.sdo.EChangeSummary;
import org.eclipse.emf.ecore.sdo.EDataGraph;
import org.eclipse.emf.ecore.sdo.util.SDOUtil;

import commonj.sdo.DataGraph;

/**
 * Update provider capable of handling EMF-based SDO data graphs.
 * 
 * @author pnehrer
 */
public class EMFUpdateProvider implements IUpdateProvider {

	private boolean debug;

	/**
	 * Sets the debug flag.
	 * 
	 * @param debug
	 * @deprecated Use Eclipse's plugin tracing support instead.
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	private EDataGraph clone(EDataGraph source) throws IOException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		source.getDataGraphResource().save(buf, null);
		return SDOUtil.loadDataGraph(
				new ByteArrayInputStream(buf.toByteArray()), null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.sdo.IUpdateProvider#createUpdate(org.eclipse.ecf.sdo.ISharedDataGraph)
	 */
	public byte[] createUpdate(ISharedDataGraph graph) throws ECFException {
		EDataGraph clone;
		try {
			clone = clone((EDataGraph) graph.getDataGraph());
		} catch (IOException e) {
			throw new ECFException(e);
		}

		EChangeSummary changes = (EChangeSummary) clone.getChangeSummary();
		changes.applyAndReverse();

		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		try {
			changes.eResource().save(buf, null);
			if (debug) {
				System.out.println("commit:");
				changes.eResource().save(System.out, null);
			}
		} catch (IOException e) {
			throw new ECFException(e);
		}

		return buf.toByteArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.sdo.IUpdateProvider#applyUpdate(org.eclipse.ecf.sdo.ISharedDataGraph,
	 *      byte[])
	 */
	public void applyUpdate(ISharedDataGraph graph, Object data)
			throws ECFException {
		EDataGraph dataGraph = (EDataGraph) graph.getDataGraph();
		EChangeSummary changeSummary = (EChangeSummary) dataGraph
				.getChangeSummary();
		changeSummary.endLogging();
		// throw away any local changes
		changeSummary.apply();

		Resource res = changeSummary.eResource();
		res.unload();

		// apply changes from the event
		try {
			res.load(new ByteArrayInputStream((byte[]) data), null);
			if (debug) {
				System.out.println("processUpdate:");
				res.save(System.out, null);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		changeSummary = (EChangeSummary) res.getContents().get(0);
		dataGraph.setEChangeSummary(changeSummary);
		// leave a change summary showing what has changed
		changeSummary.applyAndReverse();
		changeSummary.resumeLogging();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.sdo.IUpdateProvider#serializeDataGraph(commonj.sdo.DataGraph)
	 */
	public Object serializeDataGraph(DataGraph dataGraph) throws IOException {
		EDataGraph clone = clone((EDataGraph) dataGraph);
		EChangeSummary changeSummary = clone.getEChangeSummary();
		if (changeSummary != null)
			changeSummary.apply();

		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		clone.getDataGraphResource().save(buf, null);
		return buf.toByteArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.sdo.IUpdateProvider#deserializeDataGraph(Object)
	 */
	public DataGraph deserializeDataGraph(Object data) throws IOException,
			ClassNotFoundException {
		return SDOUtil.loadDataGraph(new ByteArrayInputStream((byte[]) data),
				null);
	}
}
