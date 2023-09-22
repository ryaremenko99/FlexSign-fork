package com.flexsolution.sign.exception;

/**
 * The exception is to be thrown when we can't transform the object from one format to another one
 */
public class ObjectTransformationException extends FlexCertificateException {
    public ObjectTransformationException(String fromObj, String toObj, Throwable cause) {
        super("Неможливо перетворити %s в %s".formatted(fromObj, toObj), cause);
    }
}
