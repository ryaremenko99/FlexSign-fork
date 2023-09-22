package com.flexsolution.sign.exception;

/**
 * The exception is to be thrown when we can't find the Key by the index in the signature file
 */
public class NoKeyException extends FlexCertificateException {
    public NoKeyException(int keyIndex) {
        super("Сертифікат не містить ключа за індексом %s, який би міг використовуватись для підпису".formatted(keyIndex));
    }
}
