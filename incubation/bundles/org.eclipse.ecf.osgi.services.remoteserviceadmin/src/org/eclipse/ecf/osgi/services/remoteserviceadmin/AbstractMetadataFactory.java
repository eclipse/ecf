package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import java.util.Map;

public abstract class AbstractMetadataFactory {

	protected String[] getStringArrayPropertyWithDefault(
			Map<String, Object> properties, String key,
			String[] def) {
		if (properties == null) return def;
		Object o = properties.get(key);
		if (o instanceof String) {
			return new String[] { (String) o };
		} else if (o instanceof String[]) {
			return (String[]) o;
		} else
			return def;
	}

	protected String getStringPropertyWithDefault(Map props,
			String key, String def) {
		if (props == null) return def;
		Object o = props.get(key);
		if (o == null  || (!(o instanceof String))) return def;
		return (String) o;
	}

	public void close() {
		// nothing to do
	}
}
