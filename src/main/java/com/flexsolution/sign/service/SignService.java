package com.flexsolution.sign.service;

import com.flexsolution.sign.dto.SignRequest;
import com.flexsolution.sign.util.file.FileUtils;
import com.sit.uapki.Library;
import com.sit.uapki.UapkiException;
import com.sit.uapki.common.Document;
import com.sit.uapki.common.Oids;
import com.sit.uapki.common.PkiData;
import com.sit.uapki.common.SignatureFormat;
import com.sit.uapki.key.KeyInfo;
import com.sit.uapki.key.StorageInfo;
import com.sit.uapki.method.Open;
import com.sit.uapki.method.Sign;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignService {

    private static final String SIGN_PROVIDER_PKCS_12 = "PKCS12";
    private static final int DEFAULT_KEY_INDEX = 0;

    private final Library library;

    public List<String> sign(SignRequest signRequest) throws UapkiException, IOException {
        File signatureTempFile = FileUtils.createTempFile(signRequest.getKey());

        openStorage(signatureTempFile, signRequest.getPassword());
        selectKey(DEFAULT_KEY_INDEX);
        ArrayList<Sign.DataTbs> toBeSignedBase64ContentList = prepareFilesToSign(signRequest.getFilesToSign());
        List<Document> signedDocsList = library.sign(getSignParams(), toBeSignedBase64ContentList);
        library.closeStorage();

        return signedDocsList.stream()
                .map(Document::getBytes)
                .map(PkiData::toString)
                .collect(Collectors.toList());
    }

    protected StorageInfo openStorage(File signatureFile, String password) throws UapkiException {
        return library.openStorage(SIGN_PROVIDER_PKCS_12, signatureFile.getAbsolutePath(), password, Open.Mode.RO);
    }

    private Sign.SignParams getSignParams(SignatureFormat signatureFormat) {
        Sign.SignParams signParameters = new Sign.SignParams(signatureFormat);
        signParameters.SetDetachedData(false);
        signParameters.SetIncludeCert(true);
        signParameters.SetIncludeTime(true);
        signParameters.SetIncludeContentTS(true);
        signParameters.SetSignAlgo(Oids.SignAlgo.Dstu4145.DSTU4145_WITH_GOST3411);
        return signParameters;
    }

    private ArrayList<Sign.DataTbs> prepareFilesToSign(List<String> filesToSign) {
        ArrayList<Sign.DataTbs> toBeSignedBase64ContentList = new ArrayList<>();
        for (String fileToSign : filesToSign) {
            Sign.DataTbs signDataTbs = new Sign.DataTbs(UUID.randomUUID().toString(), new PkiData(fileToSign));
            toBeSignedBase64ContentList.add(signDataTbs);
        }

        return toBeSignedBase64ContentList;
    }

    private KeyInfo selectKey(int index) throws UapkiException {
        ArrayList<KeyInfo> keyInfoList = library.getKeys();
        KeyInfo keyInfo = Optional.ofNullable(keyInfoList.get(index))
                .orElseThrow(() -> new RuntimeException("No key found"));
        library.selectKey(keyInfo.getId());
        return keyInfo;
    }
}
