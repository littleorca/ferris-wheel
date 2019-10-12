package com.ctrip.ferriswheel.core.formula;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Formula implements Iterable<FormulaElement> {
    private transient String string;
    private FormulaElement[] elements;
    private int modCount = 0;

    public Formula(String string) {
        this.string = string;
        this.elements = FormulaParser.parse(string);
    }

    public boolean isVolatile() {
        if (elements == null) {
            return false;
        }
        for (FormulaElement elem : elements) {
            if (elem.isVolatile()) {
                return true;
            }
        }
        return false;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public int getElementCount() {
        return elements.length;
    }

    public FormulaElement getElement(int index) {
        return elements[index];
    }

    @Override
    public Iterator<FormulaElement> iterator() {
        return new ElementIterator();
    }

    private class ElementIterator implements Iterator<FormulaElement> {
        private int index = 0;
        private int expectedModCount = modCount;

        @Override
        public boolean hasNext() {
            checkForComodification();
            return index < Formula.this.elements.length;
        }

        @Override
        public FormulaElement next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return Formula.this.elements[index++];
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }
}
