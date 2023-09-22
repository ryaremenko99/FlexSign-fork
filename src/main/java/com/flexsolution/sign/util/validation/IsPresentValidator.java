package com.flexsolution.sign.util.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

/**
 * Implementation for {@link IsPresent} annotation
 */
public class IsPresentValidator implements ConstraintValidator<IsPresent, MultipartFile> {

    /**
     * The method is executed when Spring validates MultipartFile that came as method parameter
     *
     * @param multipartFile MultipartFile
     * @param context ConstraintValidatorContext
     * @return true if valid, false if invalid
     */
    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext context) {
        return Objects.nonNull(multipartFile) && !multipartFile.isEmpty();
    }
}
