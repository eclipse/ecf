/****************************************************************************
* Copyright (c) 2004 Composent, Inc. and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Composent, Inc. - initial API and implementation
*****************************************************************************/

package org.eclipse.ecf.example.collab.share.url;

import java.io.File;

import org.eclipse.help.browser.IBrowser;
import org.eclipse.help.internal.browser.BrowserManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;

public class GetExec {
	private static String DEFAULT_UNIX_BROWSER = "mozilla";
	private static final String EMBEDDED_VALUE = "embedded";

	// The flag to display a url.
	private static final String UNIX_FLAG = "";
	// The flag to display a url.
	private static final String WIN_FLAG = "url.dll,FileProtocolHandler";

	// Used to identify the windows platform.
	private static final String WIN_ID = "Windows";
	// The default system browser under windows.
	private static final String WIN_PATH = "rundll32";
	protected static void displayURL(String url, boolean considerInternal) {
		boolean useEmbedded = false;
		boolean win32 = SWT.getPlatform().equals("win32");

		if (win32 && considerInternal) {
			useEmbedded = getUseEmbeddedBrowser();
		}
		if (useEmbedded) {
			IBrowser browser = BrowserManager.getInstance()
			.createBrowser(false);
			try {
				browser.displayURL(url);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			if (win32) {
				Program.launch(url);
			} else {
				// defect 11483
				IBrowser browser = BrowserManager.getInstance().createBrowser();
				try {
					browser.displayURL(url);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	public static String getBrowserExec(String unixBrowser, String url) {
		boolean windows = isWindowsPlatform();
		String cmd = null;
		if (windows)
			return WIN_PATH + " " + WIN_FLAG + " " + url;
		else {
			String browser = unixBrowser;
			if (browser == null)
				browser = DEFAULT_UNIX_BROWSER;
			return browser + " " + UNIX_FLAG + url;
		}
	}

	public static String getFileExec(String fileName) {
		if (isWindowsPlatform())
			return WIN_PATH + " " + WIN_FLAG + " " + fileName;
		else
			return fileName;
	}

	protected static boolean getUseEmbeddedBrowser() {
		return true;
	}

	public static boolean isWindowsPlatform() {
		String os = System.getProperty("os.name");
		if (os != null && os.startsWith(WIN_ID))
			return true;
		else
			return false;
	}

	public static String mangleFileName(String fileName) {
		if (fileName == null)
			return null;
		if (isWindowsPlatform())
			return fileName.replace('/', File.separatorChar).replace('|', ':');
		else
			return fileName.replace('\\', File.separatorChar);
	}
	public static void setDefaultUnixBrowser(String unixBrowser) {
		DEFAULT_UNIX_BROWSER = unixBrowser;
	}
	public static void showURL(
		final String url,
		final boolean considerInternal) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					displayURL(url, considerInternal);
				} catch (Exception e) {
					try {
						Runtime.getRuntime().exec(getBrowserExec(null, url));
					} catch (Exception e1) {
						// give up
						return;
					}
				}
			}
		});
	}

}