/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.discovery.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.ecf.internal.discovery.ui.messages"; //$NON-NLS-1$

	public static String DiscoveryView_Services;
	public static String DiscoveryView_AddressLabel;
	public static String DiscoveryView_TypeLabel;
	public static String DiscoveryView_PortLabel;
	public static String DiscoveryView_PriorityLabel;
	public static String DiscoveryView_WeightLabel;
	public static String DiscoveryView_RequestInfo;
	public static String DiscoveryView_RequestInfoTooltip;
	public static String DiscoveryView_RegisterType;
	public static String DiscoveryView_RegisterTypeTooltip;
	public static String DiscoveryView_ConnectTo;
	public static String DiscoveryView_ConnectToTooltip;
	public static String DiscoveryView_StopDiscoveryTitle;
	public static String DiscoveryView_StopDiscoveryDescription;
	public static String DiscoveryView_StopDiscovery;
	public static String DiscoveryView_StopDiscoveryTooltip;
	public static String DiscoveryView_StartDiscovery;
	public static String DiscoveryView_StartDiscoveryTooltip;
	public static String DiscoveryView_ConnectToService;
	public static String DiscoveryView_RequestInfoAboutService;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

}
