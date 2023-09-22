package com.flexsolution.sign.service;

import com.flexsolution.sign.exception.NoKeyException;
import com.sit.uapki.Library;
import com.sit.uapki.UapkiException;
import com.sit.uapki.common.PkiData;
import com.sit.uapki.key.KeyInfo;
import com.sit.uapki.method.Sign;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of methods that can be handy during implementation of CertificateService for different cert providers
 */
public abstract class AbstractCertificateService implements CertificateService {

    protected final Library library;

    public AbstractCertificateService(Library library) {
        this.library = library;
    }

    /**
     * Select key by index in the opened storage
     *
     * @param index int index of the key
     * @return KeyInfo
     * @throws UapkiException if something goes wrong
     */
    protected KeyInfo selectKey(int index) throws UapkiException {
        ArrayList<KeyInfo> keyInfoList = library.getKeys();
        KeyInfo keyInfo = Optional.ofNullable(keyInfoList.get(index))
                .orElseThrow(() -> new NoKeyException(index));
        library.selectKey(keyInfo.getId());
        return keyInfo;
    }

    /**
     * Convert File to the Array of Sign.DataTbs objects. It is required because uapki java lib demands this data format
     * for executing the SIGN function
     *
     * @param fileToBeSigned File to be signed
     * @return ArrayList<Sign.DataTbs>
     * @throws IOException if the file can't be read
     */
    protected ArrayList<Sign.DataTbs> prepareFilesToSign(File fileToBeSigned) throws IOException {
        byte[] fileToBeSignedBytes = Files.readAllBytes(fileToBeSigned.toPath());
        String fileToBeSignedBase64 = Base64.getEncoder().encodeToString(fileToBeSignedBytes);
        PkiData content = new PkiData(fileToBeSignedBase64);
        Sign.DataTbs signDataTbs = new Sign.DataTbs(UUID.randomUUID().toString(), content);
        return new ArrayList<>(Collections.singletonList(signDataTbs));
    }
}
