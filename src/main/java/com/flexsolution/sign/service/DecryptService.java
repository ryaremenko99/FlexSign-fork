package com.flexsolution.sign.service;

import com.flexsolution.sign.dto.DecryptRequest;
import com.flexsolution.sign.util.file.FileUtils;
import com.sit.uapki.Library;
import com.sit.uapki.UapkiException;
import com.sit.uapki.common.PkiData;
import com.sit.uapki.key.KeyInfo;
import com.sit.uapki.key.StorageInfo;
import com.sit.uapki.method.Decrypt;
import com.sit.uapki.method.Open;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DecryptService {

    private static final String SIGN_PROVIDER_PKCS_12 = "PKCS12";
    private static final int DEFAULT_KEY_INDEX = 0;

    private final Library library;

    public String decrypt(DecryptRequest decryptRequest) throws UapkiException, IOException {
        File signatureTempFile = FileUtils.createTempFile(decryptRequest.getKey());

        openStorage(signatureTempFile, decryptRequest.getPassword());
        selectKey(DEFAULT_KEY_INDEX);
        Decrypt.Result decrypt = library.decrypt(new PkiData(decryptRequest.getFileToDecrypt()));
        library.closeStorage();

        return decrypt.getContent().getBytes().toString();
    }

    protected StorageInfo openStorage(File signatureFile, String password) throws UapkiException {
        return library.openStorage(SIGN_PROVIDER_PKCS_12, signatureFile.getAbsolutePath(), password, Open.Mode.RO);
    }

    private KeyInfo selectKey(int index) throws UapkiException {
        ArrayList<KeyInfo> keyInfoList = library.getKeys();
        KeyInfo keyInfo = Optional.ofNullable(keyInfoList.get(index))
                .orElseThrow(() -> new RuntimeException("No key found"));
        library.selectKey(keyInfo.getId());
        return keyInfo;
    }
}
