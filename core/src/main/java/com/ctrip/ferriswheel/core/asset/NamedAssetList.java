package com.ctrip.ferriswheel.core.asset;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

class NamedAssetList<E extends NamedAssetNode> implements Iterable<E> {
    private LinkedHashMap<String, E> delegate = new LinkedHashMap<>();
    private final AssetNode owner;

    NamedAssetList(AssetNode owner) {
        this.owner = owner;
    }

    int size() {
        return delegate.size();
    }

    E get(int index) {
        Iterator<Map.Entry<String, E>> it = delegate.entrySet().iterator();
        Map.Entry<String, E> entry = null;
        for (int i = 0; i <= index; i++) {
            if (it.hasNext()) {
                entry = it.next();
            } else {
                return null;
            }
        }
        return entry == null ? null : entry.getValue();
    }

    E get(String name) {
        return delegate.get(name);
    }

    E add(E namedAsset) {
        E previous = delegate.put(namedAsset.getName(), namedAsset);
        owner.bindChild(namedAsset);
        if (previous != null) {
            owner.unbindChild(previous);
        }
        return previous;
    }

    E add(int index, E namedAsset) {
        E old = insertElement(index, namedAsset);
        owner.bindChild(namedAsset);
        if (old != null) {
            owner.unbindChild(old);
        }
        return old;
    }

    private E insertElement(int index, E namedAsset) {
        LinkedHashMap<String, E> newMap = new LinkedHashMap<>();
        Iterator<Map.Entry<String, E>> it = delegate.entrySet().iterator();
        for (int i = 0; i < index && it.hasNext(); i++) {
            Map.Entry<String, E> entry = it.next();
            newMap.put(entry.getKey(), entry.getValue());
        }
        E old = newMap.put(namedAsset.getName(), namedAsset);
        while (it.hasNext()) {
            Map.Entry<String, E> entry = it.next();
            newMap.put(entry.getKey(), entry.getValue());
        }
        delegate = newMap;
        return old;
    }

    E remove(String name) {
        E removed = delegate.remove(name);
        if (removed != null) {
            owner.unbindChild(removed);
        }
        return removed;
    }

    void clear() {
        for (E asset : delegate.values()) {
            if (asset != null) {
                owner.unbindChild(asset);
            }
        }
        delegate = new LinkedHashMap<>();
    }

    boolean rename(String fromName, String toName) {
        int idx = indexOf(fromName);
        if (idx == -1) {
            return false;
        }
        if (indexOf(toName) != -1) {
            return false; // name conflicted
        }
        E asset = delegate.remove(fromName);
        asset.setName(toName);
        insertElement(idx, asset);
        return true;
    }

    private int indexOf(String name) {
        Iterator<String> it = delegate.keySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            if (it.next().equals(name)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    @Override
    public Iterator<E> iterator() {
        return delegate.values().iterator();
    }

    public Collection<E> values() {
        return delegate.values();
    }
}
