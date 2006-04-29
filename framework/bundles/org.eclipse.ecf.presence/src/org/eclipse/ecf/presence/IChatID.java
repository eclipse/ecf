package org.eclipse.ecf.presence;

/**
 * Adapter interface for chatIDs.  The typical usage of this
 * interface is as follows:
 * <pre>
 * ID myID = ...
 * IChatID chatID = (IChatID) myID.getAdapter(IChatID.class);
 * if (chatID != null) {
 *   ...use chatID here
 * }
 * </pre>
 */
public interface IChatID {
	/**
	 * Get username for this IChatID
	 * @return String username for the implementing IChatID.  May return null.
	 */
	public String getUsername();
}
