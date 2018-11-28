package com.ctrip.ferriswheel.core.asset;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

class AssetList<E extends AssetNode> extends AbstractList<E> implements List<E> {
    private List<E> delegate = new ArrayList<>();
    private final AssetNode owner;

    AssetList(AssetNode owner) {
        this.owner = owner;
    }

    @Override
    public E get(int index) {
        return delegate.get(index);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public void add(int index, E element) {
        delegate.add(index, element);
        if (element != null) {
            owner.bindChild(element);
        }
    }

    @Override
    public E set(int index, E element) {
        E previous = super.set(index, element);
        if (element != null) {
            owner.bindChild(element);
        }
        if (previous != null) {
            owner.unbindChild(previous);
        }
        return previous;
    }

    @Override
    public E remove(int index) {
        E removed = delegate.remove(index);
        if (removed != null) {
            owner.unbindChild(removed);
        }
        return removed;
    }

    @Override
    public void clear() {
        for (E asset : delegate) {
            if (asset != null) {
                owner.unbindChild(asset);
            }
        }
        delegate = new ArrayList<>();
    }
}
