/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core;

import java.io.Serializable;
import org.eclipse.ecf.core.identity.ID;

/**
 * @author slewis
 *
 */
public interface ISharedObjectContainerGroupManager {
    /**
     * Eject the given groupMemberID from the current group of containers, for the given reason.
     * 
     * @param groupMemberID the ID of the group member to eject.  If null, or if group member is 
     * not in group managed by this object, the method has no effect
     * @param reason a reason for the ejection
     */
    public void ejectGroupMember(ID groupMemberID, Serializable reason);
}
