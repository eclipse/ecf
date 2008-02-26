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

package org.eclipse.ecf.example.collab.share;

import java.io.Serializable;

public class TreeItem implements Serializable {

	private static final long serialVersionUID = -1223990714505395727L;
	public static final String DEFAULT_SEPARATOR = ": "; //$NON-NLS-1$
	protected String label;
	protected String labelValue;
	protected String name;
	protected String separator;
	protected Object value;

	public TreeItem(String name, String labelValue) {
		this(name, labelValue, null);
	}

	public TreeItem(String name, String labelValue, Object value) {
		this(name, name, labelValue, value);
	}

	public TreeItem(String name, String label, String labelValue, Object val) {
		this(name, label, DEFAULT_SEPARATOR, labelValue, val);
	}

	public TreeItem(String name, String label, String separator,
			String labelValue, Object value) {
		this.name = name;
		this.label = label;
		this.separator = separator;
		this.labelValue = labelValue;
		this.value = value;
	}

	public boolean equals(Object o) {
		if (name == null)
			return false;
		if (o instanceof TreeItem) {
			return name.equals(((TreeItem) o).name);
		} else
			return false;
	}

	public String getLabel() {
		return label;
	}

	public String getLabelValue() {
		return labelValue;
	}

	public String getName() {
		return name;
	}

	public String getSeparator() {
		return separator;
	}

	public Object getValue() {
		return value;
	}

	public int hashCode() {
		return name.hashCode();
	}

	public void setLabelValue(String lv) {
		this.labelValue = lv;
	}

	public String toString() {
		String lv = getLabelValue();
		if (lv == null || lv.equals("")) { //$NON-NLS-1$
			return getLabel();
		} else
			return getLabel() + getSeparator() + getLabelValue();
	}

}
