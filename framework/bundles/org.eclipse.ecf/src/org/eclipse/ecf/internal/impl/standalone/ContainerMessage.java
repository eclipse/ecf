package org.eclipse.ecf.internal.impl.standalone;

import java.io.Serializable;

import org.eclipse.ecf.core.identity.ID;

public final class ContainerMessage {
	public final static byte LEAVE = 1;
	public final static byte CHANGE = 2;
	public final static byte CREATE_REPOBJ = 3;
	public final static byte CREATE_REPOBJ_RESP = 4;
	public final static byte REPOBJ_MSG = 5;
	public final static byte DESTROY_REPOBJ = 6;

	public static final class CreateResponse implements Serializable {
		static final long serialVersionUID = -1159925727012441883L;
		/**
		* @serial myObjID the RepObject ID this create response message is
		* in reference to.
		*/
		ID myObjID;
		/**
		* @serial myExcept the Exception associated with this create response
		* message.  Null if no exception generated and everything was
		* created properly.
		*/
		Throwable myExcept;
		/**
		* @serial mySeq the sequence number issued in the original create
		* message.
		*/
		long mySeq;

		public CreateResponse(ID objID, Throwable except, long sequence) {
			myObjID = objID;
			myExcept = except;
			mySeq = sequence;
		}

	}

	public static final class ContainerItemChange implements Serializable {
		static final long serialVersionUID = -491316501905217599L;
		/**
		* @serial changeIDs IDs of SharedObjectContainer group members that are part of this change message.
		*/
		ID changeIDs[];
		/**
		* @serial add boolean indicating whether this change message is an
		* add or delete.
		*/
		boolean add;
		/**
		* @serial itemData arbitrary data associated with change message.
		*/
		Serializable myData;

		ContainerItemChange(ID id, boolean a, Serializable data) {
			changeIDs = new ID[1];
			changeIDs[0] = id;
			add = a;
			myData = data;
		}

		ContainerItemChange(ID id, boolean a) {
			this(id, a, null);
		}

		ContainerItemChange(ID id[], boolean a, Serializable data) {
			changeIDs = id;
			add = a;
			myData = data;
		}

		ContainerItemChange(ID id[], boolean a) {
			this(id, a, null);
		}
	}

	public static final class SharedObjectPacket implements Serializable {
		static final long serialVersionUID = 7884246114924888824L;
		ID myFromID;
		Serializable myData;

		SharedObjectPacket(ID fromID, Serializable data) {
			myFromID = fromID;
			myData = data;
		}

	}

	public static final class SharedObjectDestroyInfo implements Serializable {
		static final long serialVersionUID = -3314198945413220488L;
		/**
		* @serial myObjID the RepObject ID that is to be destroyed in response
		* to this message.
		*/
		ID myObjID;

		SharedObjectDestroyInfo(ID objID) {
			myObjID = objID;
		}
	}

}
