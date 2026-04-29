package com.inopay;

public class InopayException extends RuntimeException {

    private final int statusCode;
    private final String errorCode;
    private final String detail;

    public InopayException(int statusCode, String errorCode, String detail) {
        super("InopayException(" + statusCode + " " + errorCode + "): " + (detail != null ? detail : ""));
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.detail = detail;
    }

    public int statusCode() { return statusCode; }
    public String errorCode() { return errorCode; }
    public String detail() { return detail; }
}
