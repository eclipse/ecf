/*
 * Created on Feb 12, 2005
 *
 */
package org.eclipse.ecf.ui.presence;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Map;
import java.util.Properties;
import org.eclipse.core.runtime.IAdaptable;

public class Presence implements Serializable, IAdaptable {
    
    protected Type type;
    protected Mode mode;
    protected int priority;
    protected String status;
    protected Map properties;
    
    public Presence() {
        this(Type.AVAILABLE);
    }
    public Presence(Type type) {
        this(type,"",Mode.AVAILABLE);
    }
    public Presence(Type type, int priority, String status, Mode mode, Map props) {
        this.type = type;
        this.priority = priority;
        this.status = status;
        this.mode = mode;
        this.properties = props;
    }
    public Presence(Type type, int priority, String status, Mode mode) {
        this(type,priority,status,mode,new Properties());
    }
    public Presence(Type type, String status, Mode mode) {
        this(type,-1,status,mode);
    }
    public Mode getMode() {
        return mode;
    }
    public int getPriority() {
        return priority;
    }
    public Map getProperties() {
        return properties;
    }
    public String getStatus() {
        return status;
    }
    public Type getType() {
        return type;
    }
    /**
     * A type-safe enum class to represent the presence type information
     *
     */
    public static class Type implements Serializable {
        
        private static final String AVAILABLE_NAME = "available";
        private static final String ERROR_NAME = "error";
        private static final String SUBSCRIBE_NAME = "subscribe";
        private static final String UNAVAILABLE_NAME = "unavailable";
        private static final String UNSUBSCRIBE_NAME = "unsubscribe";
        private static final String UNSUBSCRIBED_NAME = "unsubscribed";
        private static final String UNKWOWN_NAME = "unknown";
        
        private final transient String name;        
        // Protected constructor so that only subclasses are allowed to create instances
        protected Type(String name) {
            this.name = name;
        }
        public Type fromString(String presenceType) {
            if (presenceType == null) return null;
            if (presenceType.equals(AVAILABLE_NAME)) {
                return AVAILABLE;
            } else if (presenceType.equals(ERROR_NAME)) {
                return ERROR;
            } else if (presenceType.equals(SUBSCRIBE_NAME)) {
                return SUBSCRIBE;
            } else if (presenceType.equals(UNAVAILABLE_NAME)) {
                return UNAVAILABLE;
            } else if (presenceType.equals(UNSUBSCRIBE_NAME)) {
                return UNSUBSCRIBE;
            } else if (presenceType.equals(UNSUBSCRIBED_NAME)) {
                return UNSUBSCRIBED;
            } else if (presenceType.equals(UNKWOWN_NAME)) {
                return UNKNOWN;
            } else return null;
        }
        
        public static final Type AVAILABLE = new Type(AVAILABLE_NAME);
        public static final Type ERROR = new Type(ERROR_NAME);
        public static final Type SUBSCRIBE = new Type(SUBSCRIBE_NAME);
        public static final Type UNAVAILABLE = new Type(UNAVAILABLE_NAME);
        public static final Type UNSUBSCRIBE = new Type(UNSUBSCRIBE_NAME);
        public static final Type UNSUBSCRIBED = new Type(UNSUBSCRIBED_NAME);
        public static final Type UNKNOWN = new Type(UNKWOWN_NAME);
        
        public String toString() { return name; }
        // This is to make sure that subclasses don't screw up these methods
        public final boolean equals(Object that) {
            return super.equals(that);
        }
        public final int hashCode() {
            return super.hashCode();
        }
        // For serialization
        private static int nextOrdinal = 0;
        private final int ordinal = nextOrdinal++;
        private static final Type [] VALUES = { AVAILABLE, ERROR, SUBSCRIBE, UNAVAILABLE, UNSUBSCRIBE, UNSUBSCRIBED, UNKNOWN };
        Object readResolve() throws ObjectStreamException {
            return VALUES[ordinal];
        }
    }

    /**
     * A type-safe enum class to represent the presence mode information
     *
     */
    public static class Mode implements Serializable {
        
        private static final String AVAILABLE_NAME = "available";
        private static final String AWAY_NAME = "away";
        private static final String CHAT_NAME = "chat";
        private static final String DND_NAME = "do not disturb";
        private static final String EXTENDED_AWAY_NAME = "extended away";
        private static final String INVISIBLE_NAME = "unsubscribed";
        
        private final transient String name;        
        // Protected constructor so that only subclasses are allowed to create instances
        protected Mode(String name) {
            this.name = name;
        }
        public Mode fromString(String presenceMode) {
            if (presenceMode == null) return null;
            if (presenceMode.equals(AVAILABLE_NAME)) {
                return AVAILABLE;
            } else if (presenceMode.equals(AWAY_NAME)) {
                return AWAY;
            } else if (presenceMode.equals(CHAT_NAME)) {
                return CHAT;
            } else if (presenceMode.equals(DND_NAME)) {
                return DND;
            } else if (presenceMode.equals(EXTENDED_AWAY_NAME)) {
                return EXTENDED_AWAY;
            } else if (presenceMode.equals(INVISIBLE_NAME)) {
                return INVISIBLE;
            } else return null;
        }
        
        public static final Mode AVAILABLE = new Mode(AVAILABLE_NAME);
        public static final Mode AWAY = new Mode(AWAY_NAME);
        public static final Mode CHAT = new Mode(CHAT_NAME);
        public static final Mode DND = new Mode(DND_NAME);
        public static final Mode EXTENDED_AWAY = new Mode(EXTENDED_AWAY_NAME);
        public static final Mode INVISIBLE = new Mode(INVISIBLE_NAME);
        
        public String toString() { return name; }
        // This is to make sure that subclasses don't screw up these methods
        public final boolean equals(Object that) {
            return super.equals(that);
        }
        public final int hashCode() {
            return super.hashCode();
        }
        // For serialization
        private static int nextOrdinal = 0;
        private final int ordinal = nextOrdinal++;
        private static final Mode [] VALUES = { AVAILABLE, AWAY, CHAT, DND, EXTENDED_AWAY, INVISIBLE };
        Object readResolve() throws ObjectStreamException {
            return VALUES[ordinal];
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        return null;
    }

}
