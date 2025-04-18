package com.flexsolution.sign.service.impl;

import com.flexsolution.sign.exception.ObjectTransformationException;
import com.flexsolution.sign.exception.FlexCertificateException;
import com.flexsolution.sign.service.AbstractCertificateService;
import com.sit.uapki.Library;
import com.sit.uapki.UapkiException;
import com.sit.uapki.common.Document;
import com.sit.uapki.common.Oids;
import com.sit.uapki.common.PkiData;
import com.sit.uapki.common.SignatureFormat;
import com.sit.uapki.key.StorageInfo;
import com.sit.uapki.method.Open;
import com.sit.uapki.method.Sign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * CertificateService implementation for signatures issued by Privatbank
 */
@Service
@Slf4j
public class PrivatbankCertificateService extends AbstractCertificateService {

    private static final String SIGN_PROVIDER_PKCS_12 = "PKCS12";
    private static final int DEFAULT_KEY_INDEX = 0;

    public PrivatbankCertificateService(Library library) {
        super(library);
    }

    /**
     * Sign the document with the signature file
     *
     * @param fileToBeSigned File to be signed
     * @param signatureFile File of signature
     * @param password String password
     *
     * @return InputStream of the signed file
     *
     * @throws UapkiException if something goes wrong and uapki library can't process the signature file or sign the document
     */
    @Override
    public String sign(File fileToBeSigned,
                            File signatureFile,
                            String password) throws UapkiException {

        try {
            openStorage(signatureFile, password);
            //fixme: У більшості випадків ключ підпису генерується першим, тому має індекс 0.
            // Але універсально для коректної роботи потрібно робити наступне:
            // 1) для всіх ключів в списку робите SELECT_KEY і отримуєте ідентифікатор його сертифікату;
            // 2) отримуєте з сертифіката інформацію через CERT_INFO,
            // якщо keyUsage сертифікату не підпис або сертифікату немає - ігноруєте цей ключ;
            // 3) з усіх ключів з сертифікатом для підпису користувач вибирає потрібний (наприклад за subject.CN тощо)
            // і для нього вже робите робочий SELECT_KEY
            selectKey(DEFAULT_KEY_INDEX);
            ArrayList<Sign.DataTbs> toBeSignedBase64ContentList = prepareFilesToSign(fileToBeSigned);
            List<Document> signedDocsList = library.sign(getSignParams(), toBeSignedBase64ContentList);
            PkiData signedPkiData = signedDocsList.get(0).getBytes();
            library.closeStorage();
            return signedPkiData.toString();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ObjectTransformationException("MultipartFile", "byte[]", e);
        }
    }

    /**
     * Open file storage
     *
     * @param signatureFile File of signature
     * @param password String password
     *
     * @return StorageInfo
     */
    protected StorageInfo openStorage(File signatureFile, String password) {
        try {
            return library.openStorage(SIGN_PROVIDER_PKCS_12, signatureFile.getAbsolutePath(), password, Open.Mode.RO);
        } catch (UapkiException e) {
            log.error(e.getMessage(), e);
            throw new FlexCertificateException("Неможливо відкрити файл підпису, файл невалідний або невірний пароль");
        }
    }

    /**
     * Get default sign parameters that can be used to sign document with signature issued by Privatbank
     *
     * @return Sign.SignParams
     */
    private Sign.SignParams getSignParams() {
        Sign.SignParams signParameters = new Sign.SignParams(SignatureFormat.CADES_T);
        signParameters.SetDetachedData(false);
        signParameters.SetIncludeCert(true);
        signParameters.SetIncludeTime(true);
        signParameters.SetIncludeContentTS(true);
        signParameters.SetSignAlgo(Oids.SignAlgo.Dstu4145.DSTU4145_WITH_GOST3411);
        return signParameters;
    }
}
