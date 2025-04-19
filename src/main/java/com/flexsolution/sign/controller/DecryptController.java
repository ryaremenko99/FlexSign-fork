package com.flexsolution.sign.controller;

import com.flexsolution.sign.dto.DecryptRequest;
import com.flexsolution.sign.service.DecryptService;
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
public class DecryptController {

    private final DecryptService decryptService;

    @PostMapping(path = "/decrypt")
    public ResponseEntity<String> decrypt(@RequestBody DecryptRequest decryptRequest) throws UapkiException, IOException {
        String encrypt = decryptService.decrypt(decryptRequest);

        return ResponseEntity.ok(encrypt);
    }
}
