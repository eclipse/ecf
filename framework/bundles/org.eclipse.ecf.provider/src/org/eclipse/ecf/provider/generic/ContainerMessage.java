package org.eclipse.ecf.provider.generic;

import java.io.Serializable;
import org.eclipse.ecf.core.identity.ID;

public class ContainerMessage implements Serializable {

    ID fromContainerID;
    public ID toContainerID;
    long sequence;
    Serializable data;

    /**
     * @return Returns the data.
     */
    public Serializable getData() {
        return data;
    }
    /**
     * @param data
     *            The data to set.
     */
    public void setData(Serializable data) {
        this.data = data;
    }
    /**
     * @return Returns the fromContainerID.
     */
    public ID getFromContainerID() {
        return fromContainerID;
    }
    /**
     * @param fromContainerID
     *            The fromContainerID to set.
     */
    public void setFromContainerID(ID fromContainerID) {
        this.fromContainerID = fromContainerID;
    }
    /**
     * @return Returns the sequence.
     */
    public long getSequence() {
        return sequence;
    }
    /**
     * @param sequence
     *            The sequence to set.
     */
    public void setSequence(long sequence) {
        this.sequence = sequence;
    }
    /**
     * @return Returns the toContainerID.
     */
    public ID getToContainerID() {
        return toContainerID;
    }
    /**
     * @param toContainerID
     *            The toContainerID to set.
     */
    public void setToContainerID(ID toContainerID) {
        this.toContainerID = toContainerID;
    }
    static ContainerMessage makeViewChangeMessage(ID from, ID to, long seq,
            ID ids[], boolean add, Serializable data) {
        return new ContainerMessage(from, to, seq, new ViewChangeMessage(ids,
                add, data));
    }
    static ContainerMessage makeJoinGroupMessage(ID from, ID to, long seq,
            Serializable data) {
        return new ContainerMessage(from, to, seq, new JoinGroupMessage(data));
    }
    static ContainerMessage makeLeaveGroupMessage(ID from, ID to, long seq,
            Serializable data) {
        return new ContainerMessage(from, to, seq, new LeaveGroupMessage(data));
    }
    static ContainerMessage makeSharedObjectCreateMessage(ID from, ID to,
            long seq, Serializable data) {
        return new ContainerMessage(from, to, seq, new CreateMessage(data));
    }
    static ContainerMessage makeSharedObjectCreateResponseMessage(ID from,
            ID to, long contSeq, ID soID, Throwable e, long sequence) {
        return new ContainerMessage(from, to, contSeq,
                new CreateResponseMessage(soID, e, sequence));
    }
    static ContainerMessage makeSharedObjectMessage(ID from, ID to, long seq,
            ID fromSharedObject, Serializable data) {
        return new ContainerMessage(from, to, seq, new SharedObjectMessage(
                fromSharedObject, data));
    }
    static ContainerMessage makeSharedObjectDisposeMessage(ID from, ID to,
            long seq, ID sharedObjectID) {
        return new ContainerMessage(from, to, seq,
                new SharedObjectDisposeMessage(sharedObjectID));
    }
    protected ContainerMessage(ID from, ID to, long seq, Serializable data) {
        this.fromContainerID = from;
        this.toContainerID = to;
        this.sequence = seq;
        this.data = data;
    }

    public static final class ViewChangeMessage implements Serializable {
        ID changeIDs[];
        boolean add;
        Serializable data;
        ViewChangeMessage(ID id[], boolean a, Serializable data) {
            this.changeIDs = id;
            this.add = a;
            this.data = data;
        }
    }
    public static final class CreateMessage implements Serializable {
        Serializable data;
        CreateMessage(Serializable data) {
            this.data = data;
        }
    }
    public static final class CreateResponseMessage implements Serializable {
        ID sharedObjectID;
        Throwable exception;
        long sequence;
        public CreateResponseMessage(ID objID, Throwable except, long sequence) {
            this.sharedObjectID = objID;
            this.exception = except;
            this.sequence = sequence;
        }
    }
    public static final class SharedObjectMessage implements Serializable {
        Serializable data;
        ID fromSharedObjectID;
        SharedObjectMessage(ID fromSharedObject, Serializable data) {
            this.fromSharedObjectID = fromSharedObject;
            this.data = data;
        }
    }
    public static final class SharedObjectDisposeMessage implements
            Serializable {
        ID sharedObjectID;
        SharedObjectDisposeMessage(ID objID) {
            this.sharedObjectID = objID;
        }
    }

    public static final class JoinGroupMessage implements Serializable {
        Serializable data;

        public JoinGroupMessage(Serializable data) {
            this.data = data;
        }
        public Serializable getData() {
            return data;
        }
    }
    public static final class LeaveGroupMessage implements Serializable {
        Serializable data;

        public LeaveGroupMessage(Serializable data) {
            this.data = data;
        }
        public Serializable getData() {
            return data;
        }
    }
}