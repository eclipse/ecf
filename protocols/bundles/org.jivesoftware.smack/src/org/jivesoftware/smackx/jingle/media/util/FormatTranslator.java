package org.jivesoftware.smackx.jingle.media.util;

import java.util.ArrayList;
import java.util.Iterator;

import javax.media.Format;
import javax.sound.sampled.AudioFormat;

import org.jivesoftware.smackx.jingle.PayloadType;

import sun.misc.Service;

public class FormatTranslator {
	
	public static final float UNDEFINED_PREFERENCE_LEVEL = -1.0f;
	
	public static Object translate(Object sourceFormat, Class targetClass) {
		
		FormatTranslationProvider[] providers = getProviderArray();
		
		for(int i = 0; i < providers.length; i++) {
			Object result = providers[i].translate(sourceFormat, targetClass);
			if(result != null) return result; 
		}
		
		return null;
	}
	
	public static float preferenceLevel(PayloadType pt) {
		
		FormatTranslationProvider[] providers = getProviderArray();
		
		for(int i = 0; i < providers.length; i++) {
			float result = providers[i].preferenceLevel(pt);
			if(result != UNDEFINED_PREFERENCE_LEVEL) return result; 
		}
		
		return UNDEFINED_PREFERENCE_LEVEL;
	}

	public static AudioFormat toJSAudioFormat(Object sourceFormat) {
		return (AudioFormat) translate(sourceFormat, AudioFormat.class);
	}
	
	public static Format toJMFFormat(Object sourceFormat) {
		return (Format) translate(sourceFormat, Format.class);
	}
	
	public static PayloadType toPayloadType(Object sourceFormat) {
		return (PayloadType) translate(sourceFormat, PayloadType.class);
	}
	
	public static boolean equals(AudioFormat af1, AudioFormat af2) {
		if(!af1.getEncoding().equals(af2.getEncoding())) return false;
		if(af1.getChannels() != af2.getChannels()) return false;
		if(!af1.getClass().equals(af2.getClass())) return false;
		if(af1.getSampleSizeInBits() != af2.getSampleSizeInBits()) return false;
		if(af1.getSampleRate() != af2.getSampleRate()) return false;
		if(af1.isBigEndian() != af2.isBigEndian()) return false;
		return true;
	}
	
	public static boolean equals(javax.media.format.AudioFormat af1, javax.media.format.AudioFormat af2) {
		if(!af1.isSameEncoding(af2)) return false;
		if(af1.getChannels() != af2.getChannels()) return false;
		if(!af1.getClass().equals(af2.getClass())) return false;
		if(af1.getSampleSizeInBits() != af2.getSampleSizeInBits()) return false;
		if(af1.getSampleRate() != af2.getSampleRate()) return false;
		if(af1.getEndian() != af2.getEndian()) return false;
		if(af1.getSigned() != af2.getSigned()) return false;
		return true;
	}
	
	private static FormatTranslationProvider[] getProviderArray() {
		FormatTranslationProvider[] result = null;
		
		Iterator it = Service.providers(FormatTranslationProvider.class);
		ArrayList al = new ArrayList();
		while(it.hasNext()) al.add(it.next());
		result = (FormatTranslationProvider[]) al.toArray(new FormatTranslationProvider[0]);
		
		//if none were found use default list
		if(result.length == 0) {
			FormatTranslationProvider[] defaultList = {new DefaultFormatTranslationProvider(), new SpeexFormatTranslationProvider()};
			return defaultList;
		}
		
		return result;
	}
}

