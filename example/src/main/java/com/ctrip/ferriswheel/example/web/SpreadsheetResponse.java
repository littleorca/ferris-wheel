package com.ctrip.ferriswheel.example.web;

import java.io.Serializable;

public class SpreadsheetResponse<T extends Serializable> implements Serializable {
    public static final int STATUS_UNKNOWN_ERROR = -1;
    public static final int STATUS_OK = 0;
    public static final int STATUS_ILLEGAL_OPERATION = 1;

    private long txId;
    private int statusCode;
    private String message;
    private T content;

    public SpreadsheetResponse() {
    }

    public SpreadsheetResponse(long txId, int statusCode, String message) {
        this(txId, statusCode, message, null);
    }

    public SpreadsheetResponse(long txId, int statusCode, String message, T content) {
        this.txId = txId;
        this.statusCode = statusCode;
        this.message = message;
        this.content = content;
    }

    public long getTxId() {
        return txId;
    }

    public void setTxId(long txId) {
        this.txId = txId;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }
}