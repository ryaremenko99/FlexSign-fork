package com.flexsolution.sign.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * This object is used as a model for http error responses
 */
@Getter
@Setter
@Builder
public class HttpErrorResponseDto {
    private Date timestamp;
    private int status;
    private String message;
    private String path;
}
