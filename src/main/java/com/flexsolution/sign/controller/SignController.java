package com.flexsolution.sign.controller;

import com.flexsolution.sign.service.SignService;
import com.sit.uapki.UapkiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
public class SignController {

    private final SignService signService;

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
    public ResponseEntity<String> signDocument(
            @RequestPart MultipartFile fileToBeSigned,
            @RequestPart MultipartFile signatureFile,
            @RequestPart String password) throws UapkiException, IOException {

        String signedFile = signService.sign(fileToBeSigned, signatureFile, password);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(Objects.requireNonNull(fileToBeSigned.getContentType())))
                .body(signedFile);
    }
}
