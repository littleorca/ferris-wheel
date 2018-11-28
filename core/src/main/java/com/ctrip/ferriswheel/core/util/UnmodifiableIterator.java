package com.ctrip.ferriswheel.core.util;

import java.util.Iterator;

public class UnmodifiableIterator<E> implements Iterator<E> {
    private final Iterator delegate;

    public UnmodifiableIterator(Iterator delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean hasNext() {
        return delegate.hasNext();
    }

    @Override
    public E next() {
        return (E) delegate.next();
    }
}
