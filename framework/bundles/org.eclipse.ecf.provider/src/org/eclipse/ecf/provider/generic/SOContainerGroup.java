package org.eclipse.ecf.provider.generic;

import java.util.TreeMap;
import java.util.Collections;
import java.util.Map;
import java.util.Iterator;

public class SOContainerGroup {

    String name;
    protected Map map;

    public SOContainerGroup(String name) {
        this.name = name;
        map = Collections.synchronizedMap(new TreeMap());
    }
    public String add(String key, SOContainer aSpace) {
        if (key == null || aSpace == null)
            return null;
        map.put(key, aSpace);
        return key;
    }

    public SOContainer get(String key) {
        if (key == null)
            return null;
        return (SOContainer) map.get(key);
    }
    public SOContainer remove(String key) {
        if (key == null)
            return null;
        return (SOContainer) map.remove(key);
    }
    public boolean contains(String key) {
        if (key == null)
            return false;
        return map.containsKey(key);
    }
    public String getName() {
        return name;
    }
    public Iterator elements() {
        return map.values().iterator();
    }
}