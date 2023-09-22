package com.flexsolution.sign.exception;

/**
 * Default Parent exception for all custom exceptions implemented within the app
 */
public class FlexCertificateException extends RuntimeException {

    public FlexCertificateException(String message) {
        super(message);
    }

    public FlexCertificateException(String message, Throwable cause) {
        super(message, cause);
    }
}
