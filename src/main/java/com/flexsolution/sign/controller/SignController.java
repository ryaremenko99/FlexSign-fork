package com.flexsolution.sign.controller;

import com.flexsolution.sign.facade.ControllerCertificateFacade;
import com.flexsolution.sign.util.validation.IsPresent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Objects;

@RestController
@Validated
@Slf4j
public class SignController {

    private final ControllerCertificateFacade certificateFacade;

    public SignController(ControllerCertificateFacade certificateFacade) {
        this.certificateFacade = certificateFacade;
    }

    /**
     * This controller allows signing documents
     *
     * @param fileToBeSigned MultipartFile object of file that is going to be signed
     * @param signatureFile MultipartFile of signature file
     * @param password String password
     *
     * @return stream of signed file or JSON with details of exception if something goes wrong
     */
    @PostMapping(path = "/sign")
    public ResponseEntity<InputStreamResource> signDocument(
            @RequestPart @IsPresent MultipartFile fileToBeSigned,
            @RequestPart @IsPresent MultipartFile signatureFile,
            @RequestPart String password) {

        InputStream signedFileInputStream = certificateFacade.sign(fileToBeSigned, signatureFile, password);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(Objects.requireNonNull(fileToBeSigned.getContentType())))
                .body(new InputStreamResource(signedFileInputStream));
    }
}
