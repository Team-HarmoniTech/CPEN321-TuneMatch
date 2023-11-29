package com.cpen321.tunematch;

public class ApiException extends Exception {
    private final int statusCode;

    // ChatGPT Usage: No
    public ApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    // ChatGPT Usage: No
    public int getStatusCode() {
        return statusCode;
    }
}