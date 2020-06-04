/****************************************************************************
 * Copyright (c) 2014 Composent, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Scott Lewis - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package com.mycorp.examples.githubservice;

import java.util.concurrent.Future;

/**
 * @since 1.0
 */
public interface IGitHubServiceAsync {

	/**
	 * Gets all repositories.
	 * 
	 * @param pAccessToken
	 *            see https://help.github.com/articles/creating-an-access-token-
	 *            for-command-line-use/
	 * @return All this repositories for this access token
	 */
	public Future<String[]> getRepositoriesAsync(String pAccessToken);
}
