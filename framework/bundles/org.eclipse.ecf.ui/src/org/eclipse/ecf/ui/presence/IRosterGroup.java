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

package org.eclipse.ecf.ui.presence;

import java.util.Iterator;
import java.util.Map;

public interface IRosterGroup {
    public IRosterEntry add(IRosterEntry entry);
    public void addAll(Map existing);
    public IRosterEntry contains(IRosterEntry entry);
    public Iterator getRosterEntries();
    public int size();
    public String getName();
    public IRosterEntry removeEntry(IRosterEntry entry);
}
