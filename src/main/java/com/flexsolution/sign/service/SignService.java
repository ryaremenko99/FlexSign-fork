package com.flexsolution.sign.service;

import com.flexsolution.sign.util.file.FileHelper;
import com.sit.uapki.Library;
import com.sit.uapki.UapkiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * There are 2 reasons for why we added this level of abstraction:
 * <ul>
 *     <li>On the controller we receive MultipartFile objects, but it is a bad practice to use this object on the service layer,
 *     because in this case we won't be able to call the services from other part of the program.
 *     For example if we implement signing process from the CLI or if we decide to use a single signature file stored
 *     on the filesystem instead of reading it from the http request every time</li>
 *
 *     <li>Uapki library has a limitation, it can't sign documents with different signatures in parallel.
 *     That's why we added synchronized block here to sign the documents one by one in a single thread</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignService {

    private final CertificateService certificateService;

    /**
     * Sign document received though the controller
     *
     * @param fileToBeSigned MultipartFile the file that is required to be signed
     * @param signatureFile  MultipartFile the file of signature
     * @param password       String the password
     * @return InputStream of signed document
     */
    public String sign(MultipartFile fileToBeSigned,
                       MultipartFile signatureFile,
                       String password) throws UapkiException, IOException {

        File signatureTempFile = FileHelper.multipartFile2File(signatureFile);
        File toBeSignedTempFile = FileHelper.multipartFile2File(fileToBeSigned);

        log.info("Спроба підписати документ {} ключем {}, за допомогою сервіса {}",
                fileToBeSigned.getOriginalFilename(),
                signatureFile.getOriginalFilename(),
                certificateService.getClass().getName());
        String signedFile;
        // UAPKI бібліотека, яка лежить в основі цього застосунку, має фундаментальне обмеження -
        // вона не вміє працювати з декількома ключами одночасно. Саме тому метод, який виконує підписання має
        // виконуватись в 1 потоку.
        synchronized (Library.class) {
            signedFile = certificateService.sign(toBeSignedTempFile, signatureTempFile, password);
        }
        log.info("Файл {} успішно підписано", toBeSignedTempFile.getName());
        return signedFile;
    }
}
