package org.jivesoftware.smackx.jingle.media.util;

import org.jivesoftware.smackx.jingle.PayloadType;

public interface FormatTranslationProvider {
	
	public Object translate(Object sourceFormat, Class targetClass);
	
	public float preferenceLevel(PayloadType format);

}

