/****************************************************************************
 * Copyright (c) 2005, 2007 Remy Suen
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 *    Cagatay Calli <ccalli@gmail.com> - https://bugs.eclipse.org/bugs/show_bug.cgi?id=196812
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.protocol.msn.internal.net;

import java.io.IOException;
import java.net.*;
import org.eclipse.ecf.protocol.msn.internal.encode.StringUtils;

/**
 * The ClientTicketRequest class authenticates the user through Passport. This
 * is a necessary procedure during the NotificationSession authentication
 * process.
 */
public final class ClientTicketRequest {

	/**
	 * This String value holds the URL of the Passport Nexus page -
	 * https://nexus.passport.com/rdr/pprdr.asp
	 */
	private static final String PASSPORT_NEXUS = "https://nexus.passport.com/rdr/pprdr.asp"; //$NON-NLS-1$

	/**
	 * The connection that will be used to perform all http requests.
	 */
	private HttpURLConnection request;

	/**
	 * TODO: documentation
	 */
	private String daLoginURL;

	private boolean cancelled = false;

	/**
	 * Creates a new ClientTicketRequest object with http redirects set to true.
	 */
	public ClientTicketRequest() {
		HttpURLConnection.setFollowRedirects(true);
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	/**
	 * Retrieves information from {@link #PASSPORT_NEXUS} and stores it in
	 * {@link #daLoginURL}.
	 * 
	 * @return <code>true</code> if the retrieval process completed
	 *         successfully
	 * @throws IOException
	 *             If an I/O error occurs while attempting to connect to the
	 *             Passport Nexus page
	 */
	private boolean getLoginServerAddress() throws IOException {
		request = (HttpURLConnection) new URL(PASSPORT_NEXUS).openConnection();
		if (request.getResponseCode() == HttpURLConnection.HTTP_OK) {
			daLoginURL = StringUtils.splitSubstring(request.getHeaderField("PassportURLs"), ",", 1); //$NON-NLS-1$ //$NON-NLS-2$
			daLoginURL = "https://" //$NON-NLS-1$
					+ daLoginURL.substring(daLoginURL.indexOf('=') + 1);
			request.disconnect();
			return true;
		}
		request.disconnect();
		return false;
	}

	/**
	 * Retrieves the client ticket that is associated with the given username,
	 * password, and challenge string.
	 * 
	 * @param username
	 *            the user's email address
	 * @param password
	 *            the user's password
	 * @param challengeString
	 *            the challenge string received from the notification session
	 * @return the client ticket if login info is correct, <code>null</code> otherwise
	 * @throws IOException
	 *             If an I/O error occurs while connecting to the Passport Nexus
	 *             page or when getting the response codes from the connection
	 */
	public synchronized String getTicket(String username, String password, String challengeString) throws IOException {
		if (getLoginServerAddress()) {
			username = URLEncoder.encode(username);
			password = URLEncoder.encode(password);
			try {
				while (!cancelled) {
					request = (HttpURLConnection) new URL(daLoginURL).openConnection();
					request.setRequestProperty("Authorization", //$NON-NLS-1$
							"Passport1.4 OrgVerb=GET,OrgURL=http%3A%2F%2Fmessenger%2Emsn%2Ecom,sign-in=" //$NON-NLS-1$
									+ username + ",pwd=" + password + ',' //$NON-NLS-1$
									+ challengeString);
					if (request.getResponseCode() == HttpURLConnection.HTTP_OK) {
						password = null;
						String authenticationInfo = request.getHeaderField("Authentication-Info"); //$NON-NLS-1$
						int start = authenticationInfo.indexOf('\'');
						int end = authenticationInfo.lastIndexOf('\'');
						request.disconnect();
						return authenticationInfo.substring(start + 1, end);
					} else if (request.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
						daLoginURL = request.getHeaderField("Location"); //$NON-NLS-1$
						// truncate the uri as the received string is of the
						// form [http://www.msn.com/]
						daLoginURL = daLoginURL.substring(1, daLoginURL.length() - 1);
					} else if (request.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
						request.disconnect();
						return null;
					}
				}
			} catch (Exception e) {
				if (request.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
					return "401"; //$NON-NLS-1$
				}
				e.printStackTrace();
			} finally {
				request.disconnect();
			}
		}
		return "0"; //$NON-NLS-1$
	}
}
