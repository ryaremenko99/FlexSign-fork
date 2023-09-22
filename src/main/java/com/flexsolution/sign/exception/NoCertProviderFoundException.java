package com.flexsolution.sign.exception;

/**
 * The exception is to be thrown when app couldn't find the appropriate provider for the signature file or
 * if the password is wrong, so the file can't be read
 */
public class NoCertProviderFoundException extends FlexCertificateException {
    public NoCertProviderFoundException() {
        super("Не вдалось зчитати файл підпису, невірний пароль або відповідний провайдер не зареєстровано в системі.");
    }
}
