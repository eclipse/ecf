/****************************************************************************
 * Copyright (c) 2014 Remain BV
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Wim Jongman - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package com.mycorp.examples.githubservice;

/**
 * Example OSGi service for retrieving some data from GitHub.
 * 
 * @since 1.0
 */
public interface IGitHubService {

	/**
	 * Gets all repositories.
	 * 
	 * @param pAccessToken
	 *            see https://help.github.com/articles/creating-an-access-token-
	 *            for-command-line-use/
	 * @return All this repositories for this access token
	 */
	public String[] getRepositories(String pAccessToken);

	/**
	 * Gets all issues from repository.
	 * 
	 * @param pAccessToken
	 *            see https://help.github.com/articles/creating-an-access-token-
	 *            for-command-line-use/
	 * @param pOwner
	 *            the owner of the repository
	 * @param pRepository
	 *            the name of the repository to investigate
	 * @return All issues for the passed repository
	 */
	public String[] getIssues(String pAccessToken, String Owner, String pRepository);

}
