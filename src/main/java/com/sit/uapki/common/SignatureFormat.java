/*
 * Copyright (c) 2021, The UAPKI Project Authors.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are 
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright 
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright 
 * notice, this list of conditions and the following disclaimer in the 
 * documentation and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS 
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED 
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sit.uapki.common;

/**
 * Enumeration SignatureFormat
 */
public enum SignatureFormat {
    UNDEFINED   ("UNDEFINED"),
    CADES_BES   ("CAdES-BES"),
    CADES_T     ("CAdES-T"),
    CADES_XL     ("CAdES-XL"),
    CADES_C     ("CAdES-C"),
    CMS         ("CMS"),
    RAW         ("RAW");

    String value;

    SignatureFormat (String value) {
        this.value = value;
    }

    @Override
    public String toString () {
        return value;
    }

    public static SignatureFormat fromString (String str) {
        //  Note: switch-case is optimal solution for this enum
        //  - string contains lowerCase letters and char'-' (CADES_..)
        if (str != null) switch (str) {
            case "CAdES-BES":
                return SignatureFormat.CADES_BES;
            case "CAdES-T":
                return SignatureFormat.CADES_T;
            case "CAdES-C":
                return SignatureFormat.CADES_C;
            case "CMS":
                return SignatureFormat.CMS;
            case "RAW":
                return SignatureFormat.RAW;
            default: break;
        }
        return SignatureFormat.UNDEFINED;
    }
}
