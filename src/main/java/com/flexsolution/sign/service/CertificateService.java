package com.flexsolution.sign.service;

import com.sit.uapki.UapkiException;

import java.io.File;
import java.io.InputStream;

/**
 * Basic interface for supported actions for certificated supported by the app
 */
public interface CertificateService {

    String sign(File fileToBeSigned, File signatureFile, String password) throws UapkiException;
}
