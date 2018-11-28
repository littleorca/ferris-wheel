package com.ctrip.ferriswheel.example.web;

import java.io.Serializable;

public class SpreadsheetRequest<T extends Serializable> implements Serializable {
    private long txId;
    private T action;

    public SpreadsheetRequest() {
    }

    public SpreadsheetRequest(long txId, T action) {
        this.txId = txId;
        this.action = action;
    }

    public long getTxId() {
        return txId;
    }

    public void setTxId(long txId) {
        this.txId = txId;
    }

    public T getAction() {
        return action;
    }

    public void setAction(T action) {
        this.action = action;
    }
}