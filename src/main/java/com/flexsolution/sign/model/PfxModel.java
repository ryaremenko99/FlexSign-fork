package com.flexsolution.sign.model;

import com.sit.uapki.common.PkiOid;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PfxModel {
    private PkiOid bagCipher;
    private PkiOid bagKdf;
    private String iterations;
    private PkiOid macAlgo;
}
