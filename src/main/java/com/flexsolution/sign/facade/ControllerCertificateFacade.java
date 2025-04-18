package com.flexsolution.sign.facade;

import com.flexsolution.sign.exception.NoCertProviderFoundException;
import com.flexsolution.sign.exception.FlexCertificateException;
import com.flexsolution.sign.service.CertificateService;
import com.flexsolution.sign.util.file.FileHelper;
import com.sit.uapki.Library;
import com.sit.uapki.UapkiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Set;

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
@Service
@Slf4j
public class ControllerCertificateFacade {

    private final Set<CertificateService> certificateServiceSet;

    public ControllerCertificateFacade(Set<CertificateService> certificateServiceSet) {
        this.certificateServiceSet = certificateServiceSet;
    }

    /**
     * Sign document received though the controller
     *
     * @param fileToBeSigned MultipartFile the file that is required to be signed
     * @param signatureFile MultipartFile the file of signature
     * @param password String the password
     *
     * @return InputStream of signed document
     */
    public String sign(MultipartFile fileToBeSigned,
                     MultipartFile signatureFile,
                     String password) {


        File signatureTempFile = FileHelper.multipartFile2File(signatureFile);
        File toBeSignedTempFile = FileHelper.multipartFile2File(fileToBeSigned);

        if(log.isDebugEnabled()) {
            log.debug("Кількість зареєстрованих провайдерів у застосунку {}", certificateServiceSet.size());
        }
        for (CertificateService certificateService : certificateServiceSet) {
            try {
                if(log.isDebugEnabled()) {
                    log.debug("Спроба підписати документ {} ключем {}, за допомогою сервіса {}",
                            fileToBeSigned.getOriginalFilename(),
                            signatureFile.getOriginalFilename(),
                            certificateService.getClass().getName());
                }
                String signedFile;
                // UAPKI бібліотека, яка лежить в основі цього застосунку, має фундаментальне обмеження -
                // вона не вміє працювати з декількома ключами одночасно. Саме тому метод, який виконує підписання має
                // виконуватись в 1 потоку.
                synchronized (Library.class) {
                    signedFile = certificateService.sign(toBeSignedTempFile, signatureTempFile, password);
                }
                log.info("Файл {} успішно підписано", toBeSignedTempFile.getName());
                return signedFile;
            } catch (UapkiException | FlexCertificateException e) {
                if(log.isDebugEnabled()) {
                    log.debug("Не вдалось підписати документ за допомогою сервіса {}", certificateService.getClass().getName(), e);
                }
            }
        }

        throw new NoCertProviderFoundException();
    }
}
