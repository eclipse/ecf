package org.eclipse.ecf.provider.generic;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.provider.generic.gmm.GMMImpl;
import org.eclipse.ecf.provider.generic.gmm.Member;
import org.eclipse.ecf.provider.generic.gmm.MemberChanged;

class SOContainerGMM implements Observer {

    public static boolean DEBUG = false;

    SOContainer container;
    Member localMember;
    GMMImpl groupManager;
    // Maximum number of members. Default is -1 (no maximum).
    int maxMembers = -1;
    TreeMap loading, active;

    SOContainerGMM(SOContainer space, Member local) {
        container = space;
        groupManager = new GMMImpl();
        groupManager.addObserver(this);
        loading = new TreeMap();
        active = new TreeMap();
        localMember = local;
        addMember(local);
    }

    ID[] getSharedObjectIDs() {
        return getActiveKeys();
    }
    synchronized boolean addMember(Member m) {
        if (maxMembers > 0 && getSize() > maxMembers) {
            return false;
        } else {
            return groupManager.addMember(m);
        }
    }
    synchronized int setMaxMembers(int max) {
        int old = maxMembers;
        maxMembers = max;
        return old;
    }
    synchronized int getMaxMembers() {
        return maxMembers;
    }

    synchronized boolean removeMember(Member m) {
        boolean res = groupManager.removeMember(m);
        if (res) {
            removeSharedObjects(m);
        }
        return res;
    }

    synchronized boolean removeMember(ID id) {
        Member m = getMemberForID(id);
        if (m == null)
            return false;
        return removeMember(m);
    }

    void removeAllMembers() {
        removeAllMembers(null);
    }

    void removeNonLocalMembers() {
        removeAllMembers(localMember);
    }

    synchronized void removeAllMembers(Member exception) {
        Object m[] = getMembers();
        for (int i = 0; i < m.length; i++) {
            Member mem = (Member) m[i];
            if (exception == null || !exception.equals(mem))
                removeMember(mem);
        }
    }

    synchronized Object[] getMembers() {
        return groupManager.getMembers();
    }

    synchronized ID[] getOtherMemberIDs() {
        return groupManager.getMemberIDs(localMember.getID());
    }

    synchronized ID[] getMemberIDs() {
        return groupManager.getMemberIDs(null);
    }

    synchronized Member getMemberForID(ID id) {
        Member newMem = new Member(id);
        for (Iterator i = iterator(); i.hasNext();) {
            Member oldMem = (Member) i.next();
            if (newMem.equals(oldMem))
                return oldMem;
        }
        return null;
    }

    synchronized int getSize() {
        return groupManager.getSize();
    }

    synchronized boolean containsMember(Member m) {
        return groupManager.containsMember(m);
    }

    synchronized Iterator iterator() {
        return groupManager.iterator();
    }

    // End group membership change methods

    synchronized boolean addRepObj(SOWrapper ro) {
        if (getFromAny(ro.getObjID()) != null)
            return false;
        addSharedObjectToActive(ro);
        return true;
    }

    synchronized boolean addLoadingSharedObject(
            SOContainer.LoadingSharedObject lso) {
        if (getFromAny(lso.getID()) != null)
            return false;
        loading.put(lso.getID(), new SOWrapper(lso, container));
        // And start the thing
        lso.start();
        return true;
    }

    synchronized void moveSharedObjectFromLoadingToActive(SOWrapper ro) {
        if (removeSharedObjectFromLoading(ro.getObjID()))
            addSharedObjectToActive(ro);
    }

    boolean removeSharedObjectFromLoading(ID id) {
        if (loading.remove(id) != null) {
            return true;
        } else
            return false;
    }

    synchronized ID[] getActiveKeys() {
        return (ID[]) active.keySet().toArray(new ID[0]);
    }

    void addSharedObjectToActive(SOWrapper so) {
        ID[] ids = getActiveKeys();
        active.put(so.getObjID(), so);
        so.activated(ids);
    }

    synchronized void notifyOthersActivated(ID id) {
        notifyOtherChanged(id, active, true);
    }

    synchronized void notifyOthersDeactivated(ID id) {
        notifyOtherChanged(id, active, false);
    }

    void notifyOtherChanged(ID id, TreeMap aMap, boolean activated) {
        for (Iterator i = aMap.values().iterator(); i.hasNext();) {
            SOWrapper other = (SOWrapper) i.next();
            if (!id.equals(other.getObjID())) {
                other.otherChanged(id, activated);
            }
        }
    }

    synchronized boolean removeSharedObject(ID id) {
        SOWrapper ro = removeFromMap(id, active);
        if (ro == null)
            return false;
        ro.deactivated();
        return true;
    }

    synchronized SOWrapper getFromMap(ID objID, TreeMap aMap) {
        return (SOWrapper) aMap.get(objID);
    }

    synchronized SOWrapper removeFromMap(ID objID, TreeMap aMap) {
        return (SOWrapper) aMap.remove(objID);
    }

    SOWrapper getFromLoading(ID objID) {
        return getFromMap(objID, loading);
    }

    SOWrapper getFromActive(ID objID) {
        return getFromMap(objID, active);
    }

    synchronized SOWrapper getFromAny(ID objID) {
        SOWrapper ro = getFromMap(objID, active);
        if (ro != null)
            return ro;
        ro = getFromMap(objID, loading);
        return ro;
    }

    // Notification methods
    void notifyAllOfMemberChange(Member m, TreeMap map, boolean add) {
        for (Iterator i = map.values().iterator(); i.hasNext();) {
            SOWrapper ro = (SOWrapper) i.next();
            ro.memberChanged(m, add);
        }
    }

    public void update(Observable o, Object arg) {
        MemberChanged mc = (MemberChanged) arg;
        notifyAllOfMemberChange(mc.getMember(), active, mc.getAdded());
    }

    synchronized void removeSharedObjects(Member m) {
        removeSharedObjects(m, true);
    }

    synchronized void clear() {
        removeSharedObjects(null, true);
    }

    void removeSharedObjects(Member m, boolean match) {
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
    }

    HashSet getRemoveIDs(ID homeID, boolean match) {
        HashSet aSet = new HashSet();
        for (Iterator i = new DestroyIterator(loading, homeID, match); i
                .hasNext();) {
            aSet.add(i.next());
        }
        for (Iterator i = new DestroyIterator(active, homeID, match); i
                .hasNext();) {
            aSet.add(i.next());
        }
        return aSet;
    }

    synchronized boolean isActive(ID id) {
        return active.containsKey(id);
    }

    synchronized boolean isLoading(ID id) {
        return loading.containsKey(id);
    }

    boolean debug() {
        return DEBUG;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("RSM[");
        sb.append(groupManager);
        sb.append(";L:").append(loading);
        sb.append(";A:").append(active).append("]");
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
            SOWrapper ro = (SOWrapper) i.next();
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