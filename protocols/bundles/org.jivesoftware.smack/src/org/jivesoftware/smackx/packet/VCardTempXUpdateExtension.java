package org.jivesoftware.smackx.packet;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.util.StringUtils;

public class VCardTempXUpdateExtension implements PacketExtension {

	String photoData;
	
    public String getElementName() {
        return "x";
    }

    /** 
     * Returns the XML namespace of the extension sub-packet root element.
     * According the specification the namespace is always "http://jabber.org/protocol/xhtml-im"
     *
     * @return the XML namespace of the packet extension.
     */
    public String getNamespace() {
        return "vcard-temp:x:update";
    }

    public String toXML() {
        StringBuffer buf = new StringBuffer();
        buf.append("<").append(getElementName()).append(" xmlns=\"").append(getNamespace()).append(
            "\">");
        // Loop through all the bodies and append them to the string buffer
        buf.append("<photo>").append(getPhotoDataAsString()).append("</photo>");
        buf.append("</").append(getElementName()).append(">");
        return buf.toString();
    }

    public String getPhotoDataAsString() {
    	return photoData;
    }
    
    public byte[] getPhotoDataAsBytes() {
    	return StringUtils.decodeBase64(photoData);
    }
    
    public void setPhotoData(String data) {
    	this.photoData = data;
    }
}
