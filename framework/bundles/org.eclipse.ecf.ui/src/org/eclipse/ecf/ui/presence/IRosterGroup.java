/*
 * Created on Feb 13, 2005
 *
 */
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
