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

package org.eclipse.ecf.presence;

import java.util.Iterator;
import org.eclipse.ecf.core.identity.ID;

public interface IRosterEntry {
    
    public void add(IRosterGroup group);
    public void remove(IRosterGroup group);
    public Iterator getGroups();
    public String getName();
    public ID getUserID();
    public void setName(String name);
    public InterestType getInterestType();
    public IPresence getPresenceState();
    public void setPresenceState(IPresence presence);
    
    public static class InterestType {
        
        private static final String BOTH_NAME = "both";
        private static final String FROM_NAME = "from";
        private static final String NONE_NAME = "none";
        private static final String REMOVE_NAME = "remove";
        private static final String TO_NAME = "to";
        
        private final transient String name;        
        // Protected constructor so that only subclasses are allowed to create instances
        protected InterestType(String name) {
            this.name = name;
        }
        public static InterestType fromString(String itemType) {
            if (itemType == null) return null;
            if (itemType.equals(BOTH_NAME)) {
                return BOTH;
            } else if (itemType.equals(FROM_NAME)) {
                return FROM;
            } else if (itemType.equals(NONE_NAME)) {
                return NONE;
            } else if (itemType.equals(REMOVE_NAME)) {
                return REMOVE;
            } else if (itemType.equals(TO_NAME)) {
                return TO;
            } else return null;
        }
        
        public static final InterestType BOTH = new InterestType(BOTH_NAME);
        public static final InterestType FROM = new InterestType(FROM_NAME);
        public static final InterestType NONE = new InterestType(NONE_NAME);
        public static final InterestType REMOVE = new InterestType(REMOVE_NAME);
        public static final InterestType TO = new InterestType(TO_NAME);
        
        public String toString() { return name; }
        // This is to make sure that subclasses don't screw up these methods
        public final boolean equals(Object that) {
            return super.equals(that);
        }
        public final int hashCode() {
            return super.hashCode();
        }
    }

}
