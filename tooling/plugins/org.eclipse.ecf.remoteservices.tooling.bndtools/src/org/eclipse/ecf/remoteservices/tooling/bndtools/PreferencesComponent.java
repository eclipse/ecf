/****************************************************************************
 * Copyright (c) 2023 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.remoteservices.tooling.bndtools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true)
public class PreferencesComponent {

	@Reference
	org.eclipse.core.runtime.preferences.IPreferencesService prefs;

	static class Attrs extends HashMap<String,String> {

		private static final long serialVersionUID = -4017513476270109969L;
		
		public Attrs() {}
		
		private String name;
		
		public Attrs(Attrs a) {
			mergeWith(a);
		}
		
		void mergeWith(Attrs a) {
			if (a.name != null) {
				this.name = a.name;
			}
			for (Map.Entry<String, String> e : a.entrySet()) {
				put(e.getKey(), e.getValue());
			}			
		}
		public String toString() {
			StringBuffer sb = new StringBuffer();
			if (this.name != null) {
				sb.append("name=").append(this.name);
			}
			String del = "";
			for (Map.Entry<String, String> e : this.entrySet()) {
				sb.append(del);
				sb.append(e.getKey());
				sb.append("=");
				sb.append(e.getValue());
				del = ";";
			}
			return sb.toString();
		}
	}
	
	class Repo extends HashMap<String,Attrs> {
		
		private static final long serialVersionUID = -7604521690360522452L;

		static void parseRepo(Repo r, String s) {
			String[] repos = s.split(",");
			if (repos != null && repos.length > 0) {
				for(String repo : repos) {
					String[] parts = repo.split(";");
					if (parts != null && parts.length > 0) {
						String githuburl = parts[0].trim();
						Attrs attrs = new Attrs();
						if (parts.length > 1) {
							for (int i = 1; i < parts.length; i++) {
								String[] parts2 = parts[i].split("=");
								attrs.put(parts2[0].strip(), parts2[1].strip());
							}
						}
						r.put(githuburl, attrs);
					}				
				}
			}
		}
		
		public Repo() {
		}
		
		public Repo(String s) {
			parseRepo(this, s);
		}
		
		public void mergeWith(Repo r) {
			for (Map.Entry<String, Attrs>  e : r.entrySet()) {
				Attrs existing = get(e.getKey());
				if (existing == null) {
					put(e.getKey(), new Attrs(e.getValue()));
				} else
					existing.mergeWith(e.getValue());
			}
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			String del = "";
			for(Map.Entry<String, Attrs> e : this.entrySet()) {
				sb.append(del);
				sb.append(e.getKey());
				Attrs value = e.getValue();
				if (!value.isEmpty()) {
					sb.append(";");
					sb.append(e.getValue().toString());					
				}
				del = ",";
			}
			return sb.toString();
		}
	}
	
	private Repo getRepoParameters(InputStream ins) {
		Repo result = new Repo();
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(ins))) {
			while (reader.ready()) {
				result.mergeWith(new Repo(reader.readLine()));
			}
		} catch (Exception e) {
			// Ignore if this fails
		}
		return result;
	}
	
	@Activate
	void activate(BundleContext bundleContext) {
		List<Bundle> bundles = Arrays.asList(bundleContext.getBundles()).stream()
				.filter(b -> b.getSymbolicName().equals("org.bndtools.templating.gitrepo")).collect(Collectors.toList());
		Repo bndtoolsRepos = new Repo();
		if (bundles.size() > 0) {
			Bundle b = bundles.get(0);
			Enumeration<URL> entries = b.findEntries("org/bndtools/templating/jgit", "initialrepos.txt", false);
			if (entries != null && entries.hasMoreElements()) {
				try {
					bndtoolsRepos = getRepoParameters(entries.nextElement().openStream());
				} catch (IOException e) {
					// Ignore if can't read it
				}
			}
		}
		Repo ecfRepos = getRepoParameters(Activator.class.getResourceAsStream("ecfrepos.txt"));
		if (ecfRepos.size() > 0) {
			ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.bndtools.templating.gitrepo");
			store.setDefault("githubRepos", bndtoolsRepos.toString());
			bndtoolsRepos.mergeWith(ecfRepos);
			store.setValue("githubRepos", bndtoolsRepos.toString());
		}
	}
}
