package org.jivesoftware.smackx.jingle.media;

import java.io.InputStream;

/**
 * Jingle media listeners.
 * 
 * @author Alasdair North
 */
public interface JingleMediaListener {
	
	/**
	 * Listen for when an objects output DataSource is ready to be used.
	 * @author Alasdair North
	 */
	public static interface Output {
		
		/**
		 * Called when the objects output has changed (but not when it changes to null).
		 * @param output the new output DataSource of the listened to object.
		 */
		public void outputChanged(InputStream output);
	}
}

