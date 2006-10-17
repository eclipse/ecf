package org.jivesoftware.smackx.jingle.media.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jivesoftware.smackx.jingle.PayloadType;

public class PTIDAssigner {
	
	private static Map dynamicIDs;
	private static Set unassignedIDs;
	
	static {
		dynamicIDs = new HashMap();
		unassignedIDs = new HashSet();
		
//		initialise the Set of unassigned IDs, these are IDs in the static range that can be used
//		for dynamic payload types. Values taken from table 4 and 5 in section 6 of RFC3551
		int[] ids = {20,21,22,23,24,27,29,30,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,
				52,53,54,55,56,57,58,59,60,61,62,63,64,65,67,68,69,70,71,77,78,79,80,81,82,83,84,
				85,86,87,88,89,90,91,92,93,94,95};
		for(int i = 0; i < ids.length; i++) unassignedIDs.add(new Integer(ids[i]));
	}

	public synchronized static int getDynamicID(String name, int channels, float rate) {
		
		String lookupString = name + ", " + channels + ", " + rate;
		
//		has this pt already got an ID?
		Integer result = (Integer) dynamicIDs.get(lookupString);
		if(result != null) return result.intValue();
		
//		If not then generate a new one. Make the new ID one higher than the highest one
//		previously, or 97 if there are no previously assigned values.
		int highest = 96;
		Iterator it = dynamicIDs.values().iterator();
		while (it.hasNext()) {
			Integer current = (Integer) it.next();
			if (current.intValue() > highest) highest = current.intValue(); 
		}
		if(highest != 127) {
			dynamicIDs.put(lookupString, new Integer(highest + 1));
			return highest + 1;
		} else if(unassignedIDs.size() > 0) {
//			if the highest PT ID was 127 then we have exhausted all the available IDs in the
//			dynamic range and must start using unassigned IDs in the static range
			Integer intID = (Integer) unassignedIDs.iterator().next();
			unassignedIDs.remove(intID);
			dynamicIDs.put(lookupString, intID);
			
			return intID.intValue();
		} else return PayloadType.INVALID_PT;
	}

}

