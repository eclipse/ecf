package org.eclipse.ecf.presence.provider;

public class ProviderManager {
	
	protected static ProviderManager instance = null;
	
	static {
		instance = new ProviderManager();
	}
	
	protected ProviderManager() {
		
	}
	
	public Object addProvider(String namespace, String elementName, String type, Object provider) {
		return null;
	}
	
	public Object getProvider(String type, String namespace, String elementName) {
		return null;
	}
}
