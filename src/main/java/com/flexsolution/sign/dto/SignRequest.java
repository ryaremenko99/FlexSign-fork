package com.flexsolution.sign.dto;

import com.sit.uapki.common.SignatureFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignRequest {

    private String key;
    private String password;
    private List<String> filesToSign;
    private SignatureFormat signatureFormat;

}
