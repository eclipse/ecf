package org.eclipse.ecf.internal.impl.standalone;

import org.eclipse.ecf.core.SharedObjectNotFoundException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.internal.impl.standalone.Debug;
import org.eclipse.ecf.internal.impl.standalone.gmm.GroupManager;
import org.eclipse.ecf.internal.impl.standalone.gmm.Item;
import org.eclipse.ecf.internal.impl.standalone.gmm.ItemChange;

import java.util.Observer;
import java.util.Observable;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Iterator;

class SharedObjectContainerManager implements Observer {
	static Debug myDebug = Debug.create(SharedObjectContainerManager.class.getName());

	SharedObjectContainer container;
	Item containerPeer;
	GroupManager groupManager;
	int maxItems = -1;
	TreeMap loadingSharedObjects, activeSharedObjects;

	SharedObjectContainerManager(SharedObjectContainer space, Item ourMember) {
		container = space;
		groupManager = new GroupManager();
		groupManager.addObserver(this);
		loadingSharedObjects = new TreeMap();
		activeSharedObjects = new TreeMap();
		addItem(ourMember);
		containerPeer = ourMember;
	}

	synchronized boolean addItem(Item m) {
		if (maxItems > 0 && getSize() > maxItems) {
			return false;
		} else {
			return groupManager.addItem(m);
		}
	}
	synchronized int setMaxItems(int max) {
		int old = maxItems;
		maxItems = max;
		return old;
	}
	synchronized int getMaxItems() {
		return maxItems;
	}

	synchronized boolean removeItem(Item m) {
		boolean res = groupManager.removeItem(m);
		if (res) {
			destroySharedObjects(m);
		}
		return res;
	}

	synchronized boolean removeItem(ID id) {
		Item m = getItemForID(id);
		if (m == null)
			return false;
		return removeItem(m);
	}

	void removeAllItems() {
		removeAllItems(null);
	}

	void removeClientItems() {
		removeAllItems(containerPeer);
	}

	synchronized void removeAllItems(Item exception) {
		Object m[] = getItems();
		for (int i = 0; i < m.length; i++) {
			Item mem = (Item) m[i];
			if (exception == null || !exception.equals(mem))
				removeItem(mem);
		}
	}

	synchronized Object[] getItems() {
		return groupManager.getItems();
	}

	synchronized ID[] getOtherItemIDs() {
		return groupManager.getItemIDs(containerPeer.getID());
	}

	synchronized ID[] getItemIDs() {
		return groupManager.getItemIDs(null);
	}

	synchronized Item getItemForID(ID id) {
		Item newItem = new Item(id);
		for (Iterator i = iterator(); i.hasNext();) {
			Item old = (Item) i.next();
			if (newItem.equals(old))
				return old;
		}
		return null;
	}

	synchronized int getSize() {
		return groupManager.getSize();
	}

	synchronized boolean containsItem(Item m) {
		return groupManager.containsItem(m);
	}

	synchronized Iterator iterator() {
		return groupManager.iterator();
	}

	// End group membership change methods

	// Methods for adding/removing RepObjs
	synchronized boolean addSharedObject(SharedObjectWrapper ro) {
		// Verify that it's not already present anywhere
		if (getFromAny(ro.getObjID()) != null)
			return false;
		// Add it to active map
		addSharedObjectToActive(ro);
		// Notify ro about existing members
		return true;
	}

	synchronized boolean addToLoading(SharedObjectContainer.LoadingSharedObject lro) {
		if (getFromAny(lro.getID()) != null)
			return false;
		loadingSharedObjects.put(
			lro.getID(),
			new SharedObjectWrapper(lro.getID(), lro.getHomeID(), lro, container));
		// And start the thing
		lro.start();
		return true;
	}

	synchronized void moveFromLoadingToActive(SharedObjectWrapper ro)
		throws SharedObjectNotFoundException {
		if (removeSharedObjectFromLoading(ro.getObjID()))
			addSharedObjectToActive(ro);
	}

	boolean removeSharedObjectFromLoading(ID id) {
		if (loadingSharedObjects.remove(id) != null) {
			return true;
		} else
			return false;
	}

	synchronized ID[] getActiveKeys() {
		return (ID[]) activeSharedObjects.keySet().toArray(new ID[0]);
	}

	void addSharedObjectToActive(SharedObjectWrapper ro) {
		// Get current membership in ids array
		ID[] ids = getActiveKeys();
		// Actually add to active map
		activeSharedObjects.put(ro.getObjID(), ro);
		// Pass array of IDs to replicated object
		ro.activated(ids);
	}

	synchronized void notifyOthersActivated(ID id) {
		notifyOtherChanged(id, activeSharedObjects, true);
	}

	synchronized void notifyOthersDeactivated(ID id) {
		notifyOtherChanged(id, activeSharedObjects, false);
	}

	void notifyOtherChanged(ID id, TreeMap aMap, boolean activated) {
		for (Iterator i = aMap.values().iterator(); i.hasNext();) {
			SharedObjectWrapper other = (SharedObjectWrapper) i.next();
			if (!id.equals(other.getObjID())) {
				other.otherChanged(id, activated);
			}
		}
	}

	synchronized void destroySharedObject(ID id) throws SharedObjectNotFoundException {
		SharedObjectWrapper ro = removeFromMap(id, activeSharedObjects);
		if (ro == null)
			throw new SharedObjectNotFoundException(id + " not active");
		ro.deactivated();
	}

	synchronized SharedObjectWrapper getFromMap(ID objID, TreeMap aMap) {
		return (SharedObjectWrapper) aMap.get(objID);
	}

	synchronized SharedObjectWrapper removeFromMap(ID objID, TreeMap aMap) {
		return (SharedObjectWrapper) aMap.remove(objID);
	}

	SharedObjectWrapper getFromLoading(ID objID) {
		return getFromMap(objID, loadingSharedObjects);
	}

	SharedObjectWrapper getFromActive(ID objID) {
		return getFromMap(objID, activeSharedObjects);
	}

	synchronized SharedObjectWrapper getFromAny(ID objID) {
		SharedObjectWrapper ro = getFromMap(objID, activeSharedObjects);
		if (ro != null)
			return ro;
		ro = getFromMap(objID, loadingSharedObjects);
		return ro;
	}

	// Notification methods
	void notifyAllOfItemChange(Item m, TreeMap map, boolean add) {
		for (Iterator i = map.values().iterator(); i.hasNext();) {
			SharedObjectWrapper ro = (SharedObjectWrapper) i.next();
			ro.memberChanged(m, add);
		}
	}

	public void update(Observable o, Object arg) {
		ItemChange mc = (ItemChange) arg;
		notifyAllOfItemChange(mc.getItem(), activeSharedObjects, mc.isAdd());
	}

	synchronized void destroySharedObjects(Item m) {
		destroySharedObjects(m, true);
	}

	synchronized void clear() {
		destroySharedObjects(null, true);
	}

	void destroySharedObjects(Item m, boolean match) {
		try {
			HashSet set = getRemoveIDs(m.getID(), match);
			Iterator i = set.iterator();

			while (i.hasNext()) {
				ID removeID = (ID) i.next();
				if (isLoading(removeID)) {
					removeSharedObjectFromLoading(removeID);
				} else {
					container.destroySharedObject(removeID);
				}
			}
			
		} catch (SharedObjectNotFoundException e) {
			if (debug()) {
				myDebug.dumpStack(
					e,
					"Exception destroying for " + m + ", match: " + match);
			}
		}
	}

	HashSet getRemoveIDs(ID homeID, boolean match) {
		HashSet aSet = new HashSet();
		for (Iterator i = new DestroyIterator(loadingSharedObjects, homeID, match);
			i.hasNext();
			) {
			aSet.add(i.next());
		}
		for (Iterator i = new DestroyIterator(activeSharedObjects, homeID, match);
			i.hasNext();
			) {
			aSet.add(i.next());
		}
		return aSet;
	}

	synchronized boolean isActive(ID id) {
		return activeSharedObjects.containsKey(id);
	}

	synchronized boolean isLoading(ID id) {
		return loadingSharedObjects.containsKey(id);
	}

	boolean debug() {
		if (myDebug == null)
			return false;
		return true;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("RSM[");
		sb.append(groupManager);
		sb.append(";L:").append(loadingSharedObjects);
		sb.append(";A:").append(activeSharedObjects).append("]");
		return sb.toString();
	}

}

class DestroyIterator implements Iterator {
	ID next;
	ID homeID;
	Iterator i;
	boolean match;

	public DestroyIterator(TreeMap map, ID hID, boolean m) {
		i = map.values().iterator();
		homeID = hID;
		next = null;
		match = m;
	}

	public boolean hasNext() {
		if (next == null)
			next = getNext();
		return (next != null);
	}

	public Object next() {
		if (hasNext()) {
			ID value = next;
			next = null;
			return value;
		} else {
			throw new java.util.NoSuchElementException();
		}
	}

	ID getNext() {
		while (i.hasNext()) {
			SharedObjectWrapper ro = (SharedObjectWrapper) i.next();
			if (homeID == null || (match ^ !ro.getHomeID().equals(homeID))) {
				return ro.getObjID();
			}
		}
		return null;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}
