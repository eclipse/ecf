package org.eclipse.ecf.internal.examples.raspberrypi.management.host;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.ecf.examples.raspberrypi.management.IRaspberryPi;

public class RaspberryPi implements IRaspberryPi {

	@Override
	public Map<String, String> getSystemProperties() {
		Properties props = System.getProperties();
		
		Map<String, String> result = new HashMap<String,String>();
		for (final String name: props.stringPropertyNames())
		    result.put(name, props.getProperty(name));
		
		System.out.println("REMOTE getSystemProperties().  Returning="+result);
		
		return result;
	}

}
