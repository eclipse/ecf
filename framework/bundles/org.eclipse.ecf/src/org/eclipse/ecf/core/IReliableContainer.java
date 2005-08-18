package org.eclipse.ecf.core;

import org.eclipse.ecf.core.identity.ID;

public interface IReliableContainer extends IContainer {
	/**
	 * Get the current membership of the joined group. This method will
	 * accurately report the current group membership of the connected group.
	 * 
	 * @return ID[] the IDs of the current group membership
	 */
	public ID[] getGroupMemberIDs();

	/**
	 * @return true if this IReliableContainer instance is in the 'manager' role
	 *         for the group, false otherwise
	 */
	public boolean isGroupManager();
}
