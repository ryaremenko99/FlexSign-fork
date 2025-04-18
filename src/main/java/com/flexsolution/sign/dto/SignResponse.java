package com.flexsolution.sign.dto;

import com.sit.uapki.common.SignatureFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignResponse {

    private List<String> signedFiles;
    private SignatureFormat signatureFormat;

}
