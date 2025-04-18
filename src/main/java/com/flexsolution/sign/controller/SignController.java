package com.flexsolution.sign.controller;

import com.flexsolution.sign.dto.SignRequest;
import com.flexsolution.sign.dto.SignResponse;
import com.flexsolution.sign.service.SignService;
import com.sit.uapki.UapkiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
public class SignController {

    private final SignService signService;

    @PostMapping(path = "/sign")
    public ResponseEntity<SignResponse> signDocument(@RequestBody SignRequest signRequest) throws UapkiException, IOException {
        List<String> signedFiles = signService.sign(signRequest);
        SignResponse signResponse = SignResponse.builder()
                .signedFiles(signedFiles)
                .signatureFormat(signRequest.getSignatureFormat())
                .build();

        return ResponseEntity.ok(signResponse);
    }
}
