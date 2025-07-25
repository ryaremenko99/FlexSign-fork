package com.flexsolution.sign.controller;

import com.flexsolution.sign.dto.EncryptRequest;
import com.flexsolution.sign.service.EncryptService;
import com.sit.uapki.UapkiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class EncryptController {

    private final EncryptService encryptService;

    @PostMapping(path = "/encrypt")
    public ResponseEntity<String> encrypt(@RequestBody EncryptRequest encryptRequest) throws UapkiException, IOException {
        String encrypt = encryptService.encrypt(encryptRequest);

        return ResponseEntity.ok(encrypt);
    }
}
