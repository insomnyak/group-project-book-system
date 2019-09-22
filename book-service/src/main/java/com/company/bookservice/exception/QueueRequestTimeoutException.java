package com.company.bookservice.exception;

public class QueueRequestTimeoutException extends RuntimeException {
    public QueueRequestTimeoutException() {
    }

    public QueueRequestTimeoutException(String message) {
        super(message);
    }
}
