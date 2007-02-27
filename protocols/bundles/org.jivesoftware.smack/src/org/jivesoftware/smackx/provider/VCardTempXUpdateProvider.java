package org.jivesoftware.smackx.provider;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smackx.packet.VCardTempXUpdateExtension;
import org.xmlpull.v1.XmlPullParser;

/**
 * vCard provider.
 * 
 * @author Scott Lewis
 */
public class VCardTempXUpdateProvider implements PacketExtensionProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jivesoftware.smack.provider.PacketExtensionProvider#parseExtension(org.xmlpull.v1.XmlPullParser)
	 */
	public PacketExtension parseExtension(XmlPullParser parser)
			throws Exception {
		VCardTempXUpdateExtension photoExtension = new VCardTempXUpdateExtension();
        boolean done = false;
        StringBuffer buffer = new StringBuffer();;
        while (!done) {
            int eventType = parser.next();
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.getName().equals("photo")) 
                    buffer = new StringBuffer();
            } else if (eventType == XmlPullParser.TEXT) {
                if (buffer != null) buffer.append(parser.getText());
            } else if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals("photo")) {
                    photoExtension.setPhotoData(buffer.toString());
                }
                else if (parser.getName().equals(photoExtension.getElementName())) {
                    done = true;
                }
            }
        }

        return photoExtension;
	}

}
