/*
 * Created on Feb 13, 2005
 *
 */
package org.eclipse.ecf.ui.presence;

import java.util.Iterator;
import org.eclipse.ecf.core.identity.ID;

public interface IRosterGroup {
    public IRosterEntry add(ID entryID, IRosterEntry entry);
    public IRosterEntry contains(ID entryID);
    public Iterator getRosterEntries();
    public int size();
    public String getName();
    public IRosterEntry removeEntry(IRosterEntry entry);
}
