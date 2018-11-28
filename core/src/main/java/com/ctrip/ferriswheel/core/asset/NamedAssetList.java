package com.ctrip.ferriswheel.core.asset;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

class NamedAssetList<E extends NamedAssetNode> implements Iterable<E> {
    private LinkedHashMap<String, E> map = new LinkedHashMap<>();
    private final AssetNode delegate;

    NamedAssetList(AssetNode delegate) {
        this.delegate = delegate;
    }

    int size() {
        return map.size();
    }

    E get(int index) {
        Iterator<Map.Entry<String, E>> it = map.entrySet().iterator();
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
        return map.get(name);
    }

    E add(E namedAsset) {
        E previous = map.put(namedAsset.getName(), namedAsset);
        delegate.bindChild(namedAsset);
        if (previous != null) {
            delegate.unbindChild(previous);
        }
        return previous;
    }

    E add(int index, E namedAsset) {
        E old = insertElement(index, namedAsset);
        delegate.bindChild(namedAsset);
        if (old != null) {
            delegate.unbindChild(old);
        }
        return old;
    }

    private E insertElement(int index, E namedAsset) {
        LinkedHashMap<String, E> newMap = new LinkedHashMap<>();
        Iterator<Map.Entry<String, E>> it = map.entrySet().iterator();
        for (int i = 0; i < index && it.hasNext(); i++) {
            Map.Entry<String, E> entry = it.next();
            newMap.put(entry.getKey(), entry.getValue());
        }
        E old = newMap.put(namedAsset.getName(), namedAsset);
        while (it.hasNext()) {
            Map.Entry<String, E> entry = it.next();
            newMap.put(entry.getKey(), entry.getValue());
        }
        map = newMap;
        return old;
    }

    E remove(String name) {
        E removed = map.remove(name);
        if (removed != null) {
            delegate.unbindChild(removed);
        }
        return removed;
    }

    boolean rename(String fromName, String toName) {
        int idx = indexOf(fromName);
        if (idx == -1) {
            return false;
        }
        if (indexOf(toName) != -1) {
            return false; // name conflicted
        }
        E asset = map.remove(fromName);
        asset.setName(toName);
        insertElement(idx, asset);
        return true;
    }

    private int indexOf(String name) {
        Iterator<String> it = map.keySet().iterator();
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
        return map.values().iterator();
    }

}
