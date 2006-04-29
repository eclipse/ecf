package org.eclipse.ecf.presence;

/**
 * Adapter interface for chatIDs.  The typical usage of this
 * interface is as follows:
 * <pre>
 * IChatID chatID = (IChatID) id.getAdapter(IChatID.class);
 * if (chatID != null) {
 *   ...use chatID here
 * }
 * </pre>
 */
public interface IChatID {
	/**
	 * Get username for this ID
	 * @return String username for ID
	 */
	public String getUsername();
}
