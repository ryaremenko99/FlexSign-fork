package com.flexsolution.sign.controller;

import com.flexsolution.sign.dto.SignRequest;
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
@RestController
@Validated
@RequiredArgsConstructor
public class EncryptController {

    private final EncryptService encryptService;

    @PostMapping(path = "/encrypt")
    public ResponseEntity<String> encrypt(@RequestBody SignRequest signRequest) throws UapkiException, IOException {
        String encrypt = encryptService.encrypt(signRequest);

        return ResponseEntity.ok(encrypt);
    }
}
