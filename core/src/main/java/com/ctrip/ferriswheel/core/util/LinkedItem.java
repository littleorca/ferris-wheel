package com.ctrip.ferriswheel.core.util;

public class LinkedItem<T> {
    private T data;
    private LinkedItem<T> previous;
    private LinkedItem<T> next;

    public LinkedItem() {
    }

    public LinkedItem(T data, LinkedItem<T> previous, LinkedItem<T> next) {
        this.data = data;
        this.previous = previous;
        this.next = next;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public LinkedItem<T> getPrevious() {
        return previous;
    }

    public void setPrevious(LinkedItem<T> previous) {
        this.previous = previous;
    }

    public LinkedItem<T> getNext() {
        return next;
    }

    public void setNext(LinkedItem<T> next) {
        this.next = next;
    }
}
