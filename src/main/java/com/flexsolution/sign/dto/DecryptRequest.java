package com.flexsolution.sign.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DecryptRequest {

    private String key;
    private String password;
    private String fileToDecrypt;

}
