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

	public String toString() {
		StringBuffer sb = new StringBuffer("ContainerMessage[");
		sb.append(fromContainerID).append(";").append(toContainerID)
				.append(";");
		sb.append(sequence).append(";").append(data).append("]");
		return sb.toString();
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

		protected String printChangeIDs() {
			if (changeIDs == null)
				return "null";
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < changeIDs.length; i++) {
				buf.append(changeIDs[i]);
				if (i != (changeIDs.length - 1))
					buf.append(",");
			}
			return buf.toString();
		}

		public String toString() {
			StringBuffer sb = new StringBuffer("ViewChangeMessage[");
			sb.append(printChangeIDs()).append(";").append(add).append(";").append(data)
					.append("]");
			return sb.toString();
		}
	}

	public static final class CreateMessage implements Serializable {
		Serializable data;

		CreateMessage(Serializable data) {
			this.data = data;
		}
		public Serializable getData() {
			return data;
		}
		public String toString() {
			StringBuffer sb = new StringBuffer("CreateMessage[");
			sb.append(data).append("]");
			return sb.toString();
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

		public String toString() {
			StringBuffer sb = new StringBuffer("CreateResponseMessage[");
			sb.append(sharedObjectID).append(";").append(exception).append(";")
					.append(sequence).append("]");
			return sb.toString();
		}
		/**
		 * @return Returns the exception.
		 */
		public Throwable getException() {
			return exception;
		}
		/**
		 * @return Returns the sequence.
		 */
		public long getSequence() {
			return sequence;
		}
		/**
		 * @return Returns the sharedObjectID.
		 */
		public ID getSharedObjectID() {
			return sharedObjectID;
		}
	}

	public static final class SharedObjectMessage implements Serializable {
		Serializable data;

		ID fromSharedObjectID;

		SharedObjectMessage(ID fromSharedObject, Serializable data) {
			this.fromSharedObjectID = fromSharedObject;
			this.data = data;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer("SharedObjectMessage[");
			sb.append(fromSharedObjectID).append(";").append(data).append("]");
			return sb.toString();
		}
		/**
		 * @return Returns the data.
		 */
		public Serializable getData() {
			return data;
		}
		/**
		 * @return Returns the fromSharedObjectID.
		 */
		public ID getFromSharedObjectID() {
			return fromSharedObjectID;
		}
	}

	public static final class SharedObjectDisposeMessage implements
			Serializable {
		ID sharedObjectID;

		SharedObjectDisposeMessage(ID objID) {
			this.sharedObjectID = objID;
		}
		public String toString() {
			StringBuffer sb = new StringBuffer("SharedObjectDisposeMessage[");
			sb.append(sharedObjectID).append("]");
			return sb.toString();
		}
		/**
		 * @return Returns the sharedObjectID.
		 */
		public ID getSharedObjectID() {
			return sharedObjectID;
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
		public String toString() {
			StringBuffer sb = new StringBuffer("JoinGroupMessage[");
			sb.append(data).append("]");
			return sb.toString();
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
		public String toString() {
			StringBuffer sb = new StringBuffer("LeaveGroupMessage[");
			sb.append(data).append("]");
			return sb.toString();
		}
	}
}