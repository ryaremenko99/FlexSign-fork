package com.flexsolution.sign.config;

import com.flexsolution.sign.dto.HttpErrorResponseDto;
import com.flexsolution.sign.exception.FlexCertificateException;
import com.sit.uapki.UapkiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.WebUtils;

import java.util.Calendar;

@ControllerAdvice
@Slf4j
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handle custom exceptions and send http response in the convenient format
     * @param e exception object
     * @param request HttpServletRequest object
     *
     * @return ResponseEntity
     */
    @ExceptionHandler(value = {
            FlexCertificateException.class,
            UapkiException.class,
            ConstraintViolationException.class
    })
    protected ResponseEntity<Object> handleCertExceptions(RuntimeException e, HttpServletRequest request) {
        return buildErrorResponse(e, request.getServletPath());
    }

    /**
     * <p>Customize the response for default exceptions to have the same format as we have for custom exceptions</p>
     *
     * @param e the exception to handle
     * @param body the body to use for the response
     * @param headers the headers to use for the response
     * @param statusCode the status code to use for the response
     * @param request the current request
     * @return a {@code ResponseEntity} for the response to use, possibly
     * {@code null} when the response is already committed
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception e, @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {

        String requestPath = null;
        if (request instanceof ServletWebRequest servletWebRequest) {
            HttpServletResponse response = servletWebRequest.getResponse();
            if (response != null && response.isCommitted()) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Response already committed. Ignoring: " + e);
                }
                return null;
            }
            requestPath = servletWebRequest.getRequest().getServletPath();
        }

        if (statusCode.equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, e, WebRequest.SCOPE_REQUEST);
            return createResponseEntity(body, headers, statusCode, request);
        }

        return buildErrorResponse(e, requestPath);
    }

    /**
     * Generate response object
     * @param e exception
     * @param requestPath relative path of the executed controller, for example "/sign"
     * @return ResponseEntity
     */
    private ResponseEntity<Object> buildErrorResponse(Exception e, String requestPath) {
        HttpErrorResponseDto responseDto = HttpErrorResponseDto.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .timestamp(Calendar.getInstance().getTime())
                .path(requestPath)
                .build();
        return ResponseEntity.badRequest().body(responseDto);
    }
}
