package org.jivesoftware.smackx.jingle.media.util;

import javax.media.Format;
import javax.media.format.AudioFormat;

import org.jivesoftware.smackx.jingle.PayloadType;

public class DefaultFormatTranslationProvider implements FormatTranslationProvider {
	
	private static final AudioFormat JMF_STATIC_PT_0 = new AudioFormat(AudioFormat.ULAW_RTP, 8000.0, 8, 1,
			AudioFormat.LITTLE_ENDIAN, AudioFormat.NOT_SPECIFIED, 8,
			AudioFormat.NOT_SPECIFIED, byte[].class);
	
	private static final AudioFormat JMF_STATIC_PT_3 = new AudioFormat(AudioFormat.GSM_RTP, 8000.0, AudioFormat.NOT_SPECIFIED,
			1, AudioFormat.NOT_SPECIFIED, AudioFormat.NOT_SPECIFIED, 264,
			AudioFormat.NOT_SPECIFIED, byte[].class);
	
	private static final AudioFormat JMF_STATIC_PT_4 = new AudioFormat(AudioFormat.G723_RTP, 8000.0, AudioFormat.NOT_SPECIFIED,
			1, AudioFormat.NOT_SPECIFIED, AudioFormat.NOT_SPECIFIED, 192,
			AudioFormat.NOT_SPECIFIED, byte[].class);
	
	private static final AudioFormat JMF_STATIC_PT_5 = new AudioFormat(AudioFormat.DVI_RTP, 8000.0, 4, 1);
	
	private static final AudioFormat JMF_STATIC_PT_6 = new AudioFormat(AudioFormat.DVI_RTP, 16000.0, 4, 1);
	
	private static final AudioFormat JMF_STATIC_PT_8 = new AudioFormat(AudioFormat.ALAW, 8000.0, 8, 1,
			AudioFormat.LITTLE_ENDIAN, AudioFormat.NOT_SPECIFIED, 8,
			AudioFormat.NOT_SPECIFIED, byte[].class);
	
	private static final AudioFormat JMF_STATIC_PT_10 = new AudioFormat(AudioFormat.LINEAR, 44100.0, 16, 2,
			AudioFormat.BIG_ENDIAN, AudioFormat.SIGNED, AudioFormat.NOT_SPECIFIED,
			AudioFormat.NOT_SPECIFIED, byte[].class);
	
	private static final AudioFormat JMF_STATIC_PT_11 = new AudioFormat(AudioFormat.LINEAR, 44100.0, 16, 1,
			AudioFormat.BIG_ENDIAN, AudioFormat.SIGNED, AudioFormat.NOT_SPECIFIED,
			AudioFormat.NOT_SPECIFIED, byte[].class);
		
	private static final AudioFormat JMF_STATIC_PT_14 = new AudioFormat(AudioFormat.MPEG_RTP, 44100.0, 16, 1);
			
	private static final AudioFormat JMF_STATIC_PT_16 = new AudioFormat(AudioFormat.DVI_RTP, 11025.0, 4, 1);
				
	private static final AudioFormat JMF_STATIC_PT_17 = new AudioFormat(AudioFormat.DVI_RTP, 22050.0, 4, 1);
	
	
	private static final javax.sound.sampled.AudioFormat JS_STATIC_PT_0 = new javax.sound.sampled.AudioFormat(javax.sound.sampled.AudioFormat.Encoding.ULAW, 8000f, 8, 1, 1, 8000f, false);
	
	private static final javax.sound.sampled.AudioFormat JS_STATIC_PT_8 = new javax.sound.sampled.AudioFormat(javax.sound.sampled.AudioFormat.Encoding.ALAW, 8000f, 8, 1, 1, 8000f, false);
	
	private static final javax.sound.sampled.AudioFormat JS_STATIC_PT_10 = new javax.sound.sampled.AudioFormat(44100f, 16, 2, true, true);
	
	private static final javax.sound.sampled.AudioFormat JS_STATIC_PT_11 = new javax.sound.sampled.AudioFormat(44100f, 16, 1, true, true);
	

	public Object translate(Object sourceFormat, Class targetClass) {
		if(targetClass == AudioFormat.class || targetClass == Format.class) {
			if(sourceFormat instanceof PayloadType) return toJMFAudioFormat((PayloadType) sourceFormat);
			else if(sourceFormat instanceof javax.sound.sampled.AudioFormat) return toJMFAudioFormat((javax.sound.sampled.AudioFormat) sourceFormat);
			else if(sourceFormat instanceof AudioFormat) return sourceFormat;
			
		} else if(targetClass == javax.sound.sampled.AudioFormat.class) {
			if(sourceFormat instanceof PayloadType) return toJSAudioFormat((PayloadType) sourceFormat);
			else if(sourceFormat instanceof AudioFormat) return toJSAudioFormat((AudioFormat) sourceFormat);
			else if(sourceFormat instanceof javax.sound.sampled.AudioFormat) return sourceFormat;
			
		} else if(targetClass == PayloadType.class || targetClass == PayloadType.Audio.class) {
			if(sourceFormat instanceof javax.sound.sampled.AudioFormat) return toPayloadType((javax.sound.sampled.AudioFormat) sourceFormat);
			else if(sourceFormat instanceof AudioFormat) return toPayloadType((AudioFormat) sourceFormat);
			else if(sourceFormat instanceof PayloadType) return sourceFormat;
		}
		
		return null;
	}

	private AudioFormat toJMFAudioFormat(PayloadType pt) {
		AudioFormat result = null;
		
		//first the static payload types
		switch(pt.getId()) {
		case 0: result = JMF_STATIC_PT_0; break;
		case 3: result = JMF_STATIC_PT_3; break;
		case 4: result = JMF_STATIC_PT_4; break;
		case 5: result = JMF_STATIC_PT_5; break;
		case 6: result = JMF_STATIC_PT_6; break;
		case 8: result = JMF_STATIC_PT_8; break;
		case 10: result = JMF_STATIC_PT_10; break;
		case 11: result = JMF_STATIC_PT_11; break;
		case 14: result = JMF_STATIC_PT_14; break;
		case 16: result = JMF_STATIC_PT_16; break;
		case 17: result = JMF_STATIC_PT_17; break;
		}
		
		//then the dynamic ones
		if(result == null) {
			if(!(pt instanceof PayloadType.Audio)) throw new Error("PayloadType had dynamic ID but was" +
					" not an instance of PayloadType.Audio and so did not contain enough" +
					" information.");
			PayloadType.Audio pta = (PayloadType.Audio) pt;
			
			if(pta.getClockRate() == 0) throw new Error("Sampling rate was" +
				" not set in the given PayloadType.");
			if(pta.getName() == null) throw new Error("Codec name was" +
				" not set in the given PayloadType");
			
			if(pta.getName().equals("PCMA")) {
				result = new AudioFormat(AudioFormat.ALAW, pta.getClockRate(), 8, pta.getChannels(),
						AudioFormat.LITTLE_ENDIAN, AudioFormat.NOT_SPECIFIED, 8,
						AudioFormat.NOT_SPECIFIED, byte[].class);
				
			} else if(pta.getName().equals("PCMU")) {
				result = new AudioFormat(AudioFormat.ULAW_RTP, pta.getClockRate(), 8, pta.getChannels(),
						AudioFormat.LITTLE_ENDIAN, AudioFormat.NOT_SPECIFIED, 8,
						AudioFormat.NOT_SPECIFIED, byte[].class);
				
			} else if(pta.getName().equals("L16")) {
				result = new AudioFormat(AudioFormat.LINEAR, pta.getClockRate(), 16, pta.getChannels(),
						AudioFormat.BIG_ENDIAN, AudioFormat.SIGNED, AudioFormat.NOT_SPECIFIED,
						AudioFormat.NOT_SPECIFIED, byte[].class);
				
			} else if(pta.getName().equals("L8")) {
				result = new AudioFormat(AudioFormat.LINEAR, pta.getClockRate(), 8, pta.getChannels(),
						AudioFormat.BIG_ENDIAN, AudioFormat.SIGNED, AudioFormat.NOT_SPECIFIED,
						AudioFormat.NOT_SPECIFIED, byte[].class);
				
			} else if(pta.getName().equals("MPA")) {
				result = new AudioFormat(AudioFormat.MPEG_RTP, pta.getClockRate(), 16, pta.getChannels());
				
			} else if(pta.getName().equals("GSM")) {
				result = new AudioFormat(AudioFormat.GSM_RTP, pta.getClockRate(), AudioFormat.NOT_SPECIFIED,
						pta.getChannels(), AudioFormat.NOT_SPECIFIED, AudioFormat.NOT_SPECIFIED, 264,
						AudioFormat.NOT_SPECIFIED, byte[].class);
				
			} else if(pta.getName().equals("G723")) {
				result = new AudioFormat(AudioFormat.G723_RTP, pta.getClockRate(), AudioFormat.NOT_SPECIFIED,
						pta.getChannels(), AudioFormat.NOT_SPECIFIED, AudioFormat.NOT_SPECIFIED, 192,
						AudioFormat.NOT_SPECIFIED, byte[].class);
				
			} else if(pta.getName().equals("DVI4")) {
				result = new AudioFormat(AudioFormat.DVI_RTP, pta.getClockRate(), 4, pta.getChannels());
			}
		}
		
		return result;
	}
	
	private AudioFormat toJMFAudioFormat(javax.sound.sampled.AudioFormat af) {
		
		if(af.getEncoding().equals(javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED)) {
			int endianness = af.isBigEndian() ? AudioFormat.BIG_ENDIAN : AudioFormat.LITTLE_ENDIAN;
			
			return new AudioFormat(AudioFormat.LINEAR, af.getSampleRate(), af.getSampleSizeInBits(), af.getChannels(),
					endianness, AudioFormat.SIGNED, AudioFormat.NOT_SPECIFIED,
					AudioFormat.NOT_SPECIFIED, byte[].class);
			
		} else if(af.getEncoding().equals(javax.sound.sampled.AudioFormat.Encoding.PCM_UNSIGNED)) {
			int endianness = af.isBigEndian() ? AudioFormat.BIG_ENDIAN : AudioFormat.LITTLE_ENDIAN;
			
			return new AudioFormat(AudioFormat.LINEAR, af.getSampleRate(), af.getSampleSizeInBits(), af.getChannels(),
					endianness, AudioFormat.UNSIGNED, AudioFormat.NOT_SPECIFIED,
					AudioFormat.NOT_SPECIFIED, byte[].class);
			
		} else {
			PayloadType pt = toPayloadType(af);
			if(pt != null) return toJMFAudioFormat(pt);
			return null;
		}
	}
	
	private javax.sound.sampled.AudioFormat toJSAudioFormat(PayloadType pt) {
		//Static payload types
		switch(pt.getId()) {
		case 0: return JS_STATIC_PT_0;
		case 8: return JS_STATIC_PT_8;
		case 10: return JS_STATIC_PT_10;
		case 11: return JS_STATIC_PT_11;
		}
		
		//Dynamic payload types
		if(pt instanceof PayloadType.Audio) {
			PayloadType.Audio pta = (PayloadType.Audio) pt;
			if(pta.getName() != null && pta.getChannels() != 0 && pta.getClockRate() != 0f) {
				if(pta.getName().equals("PCMU")) {
					return new javax.sound.sampled.AudioFormat(javax.sound.sampled.AudioFormat.Encoding.ULAW, pta.getClockRate(), 8, pta.getChannels(), pta.getChannels(), pta.getClockRate(), false);
				} else if(pta.getName().equals("PCMA")) {
					return new javax.sound.sampled.AudioFormat(javax.sound.sampled.AudioFormat.Encoding.ALAW, pta.getClockRate(), 8, pta.getChannels(), pta.getChannels(), pta.getClockRate(), false);
				} else if(pta.getName().equals("L16")) {
					return new javax.sound.sampled.AudioFormat(pta.getClockRate(), 16, pta.getChannels(), true, true);
				} else if(pta.getName().equals("L8")) {
					return new javax.sound.sampled.AudioFormat(pta.getClockRate(), 8, pta.getChannels(), true, true);
				}
			}
		}
		
		return null;
	}
	
	private javax.sound.sampled.AudioFormat toJSAudioFormat(AudioFormat af) {
		if(af.getEncoding().equals(AudioFormat.LINEAR)) {
			return new javax.sound.sampled.AudioFormat((float) af.getSampleRate(), af.getSampleSizeInBits(), af.getChannels(),
					af.getSigned() == AudioFormat.SIGNED, af.getEndian() == AudioFormat.BIG_ENDIAN);
		} else {
			PayloadType pt = toPayloadType(af);
			if(pt != null) return toJSAudioFormat(pt);
			return null;
		}
	}
	
	private PayloadType toPayloadType(AudioFormat af) {
		
		//static payload types
		if((FormatTranslator.equals(af, JMF_STATIC_PT_0))) return new PayloadType(0, "PCMU");
		if((FormatTranslator.equals(af, JMF_STATIC_PT_3))) return new PayloadType(3, "GSM");
		if((FormatTranslator.equals(af, JMF_STATIC_PT_4))) return new PayloadType(4, "G723");
		if((FormatTranslator.equals(af, JMF_STATIC_PT_5))) return new PayloadType(5, "DVI4");
		if((FormatTranslator.equals(af, JMF_STATIC_PT_6))) return new PayloadType(6, "DVI4");
		if((FormatTranslator.equals(af, JMF_STATIC_PT_8))) return new PayloadType(8, "PCMA");
		if((FormatTranslator.equals(af, JMF_STATIC_PT_10))) return new PayloadType(10, "L16");
		if((FormatTranslator.equals(af, JMF_STATIC_PT_11))) return new PayloadType(11, "L16");
		if((FormatTranslator.equals(af, JMF_STATIC_PT_14))) return new PayloadType(14, "MPA");
		if((FormatTranslator.equals(af, JMF_STATIC_PT_16))) return new PayloadType(16, "DVI4");
		if((FormatTranslator.equals(af, JMF_STATIC_PT_17))) return new PayloadType(17, "DVI4");
		
		//dynamic payload types
		String name = "";
		if(af.getEncoding().equals(AudioFormat.ALAW) && af.getSampleSizeInBits() == 8) name = "PCMA";
		else if(af.getEncoding().equals(AudioFormat.GSM_RTP) && af.getFrameSizeInBits() == 264) name = "GSM";
		else if(af.getEncoding().equals(AudioFormat.G723_RTP) && af.getFrameSizeInBits() == 192) name = "G723";
		else if((af.getEncoding().equals(AudioFormat.DVI) || af.getEncoding().equals(AudioFormat.DVI_RTP)) && af.getSampleSizeInBits() == 4) name = "DVI4";
		else if((af.getEncoding().equals(AudioFormat.ULAW) || af.getEncoding().equals(AudioFormat.ULAW_RTP)) && af.getSampleSizeInBits() == 8) name = "PCMU";
		else if(af.getEncoding().equals(AudioFormat.LINEAR) && af.getSampleSizeInBits() == 16 && af.getEndian() == AudioFormat.BIG_ENDIAN && af.getSigned() == AudioFormat.SIGNED) name = "L16";
		else if(af.getEncoding().equals(AudioFormat.LINEAR) && af.getSampleSizeInBits() == 8 && af.getEndian() == AudioFormat.BIG_ENDIAN && af.getSigned() == AudioFormat.SIGNED) name = "L8";
		else if((af.getEncoding().equals(AudioFormat.MPEG) || af.getEncoding().equals(AudioFormat.MPEG_RTP)) && af.getSampleSizeInBits() == 16) name = "MPA";
		
		if(name != "") {
			int id = PTIDAssigner.getDynamicID(name, af.getChannels(), (float) af.getSampleRate());
			return new PayloadType.Audio(id, name, af.getChannels(), (int) Math.round(af.getSampleRate()));
		}
		
		return null;
	}
	
	private PayloadType toPayloadType(javax.sound.sampled.AudioFormat af) {
//		Static payload types
		if(FormatTranslator.equals(af, JS_STATIC_PT_0)) return new PayloadType(0, "PCMU");
		if(FormatTranslator.equals(af, JS_STATIC_PT_8)) return new PayloadType(8, "PCMA");
		if(FormatTranslator.equals(af, JS_STATIC_PT_10)) return new PayloadType(10, "L16");
		if(FormatTranslator.equals(af, JS_STATIC_PT_11)) return new PayloadType(11, "L16");
		
		//Dynamic payload types
		String name = "";
		if(af.getEncoding().equals(javax.sound.sampled.AudioFormat.Encoding.ULAW) && af.getSampleSizeInBits() == 8) name = "PCMU";
		else if(af.getEncoding().equals(javax.sound.sampled.AudioFormat.Encoding.ALAW) && af.getSampleSizeInBits() == 8) name = "PCMA";
		else if(af.getEncoding().equals(javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED) && af.getSampleSizeInBits() == 16 && af.isBigEndian()) name = "L16";
		else if(af.getEncoding().equals(javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED) && af.getSampleSizeInBits() == 8 && af.isBigEndian()) name = "L8";
		
		if(name != "") {
			int id = PTIDAssigner.getDynamicID(name, af.getChannels(), af.getSampleRate());
			return new PayloadType.Audio(id, name, af.getChannels(), Math.round(af.getSampleRate()));
		}
		
		return null;
	}

	public float preferenceLevel(PayloadType pt) {
		// TODO Auto-generated method stub
		return 0;
	}
}

