package org.jivesoftware.smackx.jingle;

/**
 * Content info. Content info messages are complementary messages that can be
 * transmitted for informing of events like "busy", "ringtone", etc.
 * 
 * @author Alvaro Saurin <alvaro.saurin@gmail.com>
 */
public abstract class ContentInfo {

	/**
	 * Audio conten info messages.
	 * 
	 * @author Alvaro Saurin <alvaro.saurin@gmail.com>
	 */
	public static class Audio extends ContentInfo {

		public static final ContentInfo.Audio BUSY = new ContentInfo.Audio("busy");

		public static final ContentInfo.Audio HOLD = new ContentInfo.Audio("hold");

		public static final ContentInfo.Audio MUTE = new ContentInfo.Audio("mute");

		public static final ContentInfo.Audio QUEUED = new ContentInfo.Audio("queued");

		public static final ContentInfo.Audio RINGING = new ContentInfo.Audio("ringing");

		private String value;

		public Audio(final String value) {
			this.value = value;
		}

		public String toString() {
			return value;
		}

		/**
		 * Returns the MediaInfo constant associated with the String value.
		 */
		public static ContentInfo fromString(String value) {
			value = value.toLowerCase();
			if (value.equals("busy")) {
				return BUSY;
			} else if (value.equals("hold")) {
				return HOLD;
			} else if (value.equals("mute")) {
				return MUTE;
			} else if (value.equals("queued")) {
				return QUEUED;
			} else if (value.equals("ringing")) {
				return RINGING;
			} else {
				return null;
			}
		}
	}
}
