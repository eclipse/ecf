package org.jivesoftware.smackx.jingle.media.util;

//import javax.media.Format;
//import javax.media.format.AudioFormat;
//import javax.sound.sampled.AudioSystem;

import org.jivesoftware.smackx.jingle.PayloadType;
//import org.xiph.speex.spi.SpeexEncoding;

public class SpeexFormatTranslationProvider implements FormatTranslationProvider {
	
//	This class is mostly commented out to disable Speex in Jingle media. This is done because
//	at the moment Speex decoding and encoding doesn't quite work. Once this is fixed then this
//	code can all be uncommented to reenable Speex support.
	
//                                                      0      1      2      3      4      5      6      7      8      9      10
//	private static final boolean[] non_vbr_qualities = {false, false, false, true, false, false, false, true, false, false, true};
//	private static final boolean[]     vbr_qualities = {false, false, false, true, false, false, false, true, false, false, true};

	public Object translate(Object sourceFormat, Class targetClass) {
/*		if(targetClass == AudioFormat.class || targetClass == Format.class) {
			if(sourceFormat instanceof javax.sound.sampled.AudioFormat)
				return toJMFAudioFormat((javax.sound.sampled.AudioFormat) sourceFormat);
						
		} else if(targetClass == javax.sound.sampled.AudioFormat.class) {
			if(sourceFormat instanceof PayloadType.Audio)
				return toJSAudioFormat((PayloadType.Audio) sourceFormat);
			
			else if(sourceFormat instanceof AudioFormat)
				return toJSAudioFormat((AudioFormat) sourceFormat);
						
		} else if(targetClass == PayloadType.class || targetClass == PayloadType.Audio.class) {
			if(sourceFormat instanceof javax.sound.sampled.AudioFormat)
				return toPayloadType((javax.sound.sampled.AudioFormat) sourceFormat);
		}
		
*/		return null;
	}

/*	private AudioFormat toJMFAudioFormat(javax.sound.sampled.AudioFormat af) {
		if(af.getEncoding() instanceof SpeexEncoding && allowed(af.getEncoding().toString())) {
			return new AudioFormat(af.getEncoding().toString(), af.getSampleRate(), AudioFormat.NOT_SPECIFIED, af.getChannels());
		}
		return null;
	}
	
	private javax.sound.sampled.AudioFormat toJSAudioFormat(PayloadType.Audio pta) {
		if(pta.getName().startsWith("SPEEX") && allowed(pta.getName())) {
			SpeexEncoding encoding  = getEncoding(pta.getName());
			return new javax.sound.sampled.AudioFormat(encoding, pta.getClockRate(), AudioSystem.NOT_SPECIFIED,
					pta.getChannels(), AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, false);
		}
		return null;
	}
	
	private javax.sound.sampled.AudioFormat toJSAudioFormat(AudioFormat af) {
		if(af.getEncoding().startsWith("SPEEX") && allowed(af.getEncoding())) {
			SpeexEncoding encoding  = getEncoding(af.getEncoding());
			if(encoding != null) return new javax.sound.sampled.AudioFormat(encoding, (float) af.getSampleRate(), AudioSystem.NOT_SPECIFIED,
					af.getChannels(), AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, false);
		}
		return null;
	}
	
	private PayloadType toPayloadType(javax.sound.sampled.AudioFormat af) {
		if(af.getEncoding() instanceof SpeexEncoding && allowed(af.getEncoding().toString())) {
			int id = PTIDAssigner.getDynamicID(af.getEncoding().toString(), af.getChannels(), af.getSampleRate());
			return new PayloadType.Audio(id, af.getEncoding().toString(), af.getChannels(), Math.round(af.getSampleRate()));
		}
		return null;
	}*/

	public float preferenceLevel(PayloadType pt) {
		if(!pt.getName().startsWith("SPEEX"))
			return FormatTranslator.UNDEFINED_PREFERENCE_LEVEL;
		else {
			
		}
		return FormatTranslator.UNDEFINED_PREFERENCE_LEVEL;
	}
	
/*	private static SpeexEncoding getEncoding(String speexString) {
		SpeexEncoding speexEnc = null;
		
		if(allowed(speexString)) {
			if(speexString.equals("SPEEX_quality_0")) speexEnc = SpeexEncoding.SPEEX_Q0;
			else if(speexString.equals("SPEEX_quality_1")) speexEnc = SpeexEncoding.SPEEX_Q1;
			else if(speexString.equals("SPEEX_quality_2")) speexEnc = SpeexEncoding.SPEEX_Q2;
			else if(speexString.equals("SPEEX_quality_3")) speexEnc = SpeexEncoding.SPEEX_Q3;
			else if(speexString.equals("SPEEX_quality_4")) speexEnc = SpeexEncoding.SPEEX_Q4;
			else if(speexString.equals("SPEEX_quality_5")) speexEnc = SpeexEncoding.SPEEX_Q5;
			else if(speexString.equals("SPEEX_quality_6")) speexEnc = SpeexEncoding.SPEEX_Q6;
			else if(speexString.equals("SPEEX_quality_7")) speexEnc = SpeexEncoding.SPEEX_Q7;
			else if(speexString.equals("SPEEX_quality_8")) speexEnc = SpeexEncoding.SPEEX_Q8;
			else if(speexString.equals("SPEEX_quality_9")) speexEnc = SpeexEncoding.SPEEX_Q9;
			else if(speexString.equals("SPEEX_quality_10")) speexEnc = SpeexEncoding.SPEEX_Q10;
			else if(speexString.equals("SPEEX_VBR_quality_0")) speexEnc = SpeexEncoding.SPEEX_VBR0;
			else if(speexString.equals("SPEEX_VBR_quality_1")) speexEnc = SpeexEncoding.SPEEX_VBR1;
			else if(speexString.equals("SPEEX_VBR_quality_2")) speexEnc = SpeexEncoding.SPEEX_VBR2;
			else if(speexString.equals("SPEEX_VBR_quality_3")) speexEnc = SpeexEncoding.SPEEX_VBR3;
			else if(speexString.equals("SPEEX_VBR_quality_4")) speexEnc = SpeexEncoding.SPEEX_VBR4;
			else if(speexString.equals("SPEEX_VBR_quality_5")) speexEnc = SpeexEncoding.SPEEX_VBR5;
			else if(speexString.equals("SPEEX_VBR_quality_6")) speexEnc = SpeexEncoding.SPEEX_VBR6;
			else if(speexString.equals("SPEEX_VBR_quality_7")) speexEnc = SpeexEncoding.SPEEX_VBR7;
			else if(speexString.equals("SPEEX_VBR_quality_8")) speexEnc = SpeexEncoding.SPEEX_VBR8;
			else if(speexString.equals("SPEEX_VBR_quality_9")) speexEnc = SpeexEncoding.SPEEX_VBR9;
			else if(speexString.equals("SPEEX_VBR_quality_10")) speexEnc = SpeexEncoding.SPEEX_VBR10;
		}
		
		return speexEnc;
	}
	
	private static boolean allowed(String speexString) {
		String[] split = speexString.split("_");
		if(split.length == 3) {
			int index = Integer.parseInt(split[2]);
			return non_vbr_qualities[index];
		} else if (split.length == 4) {
			int index = Integer.parseInt(split[3]);
			return vbr_qualities[index];
		} else return false;
			
	}
*/}


