package com.flexsolution.sign.util.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>Custom annotation @IsPresent for MultipartFile in order to be validated by Spring Validator.</p>
 * <p>This validation is required for cases when parameter is present in the body but the value is empty.
 * The @RequestPart annotation validates only whether parameter is present in the body or not.</p>
 *
 */
@Constraint(validatedBy = IsPresentValidator.class)
@Target({ElementType.PARAMETER})
@Retention(RUNTIME)
@Documented
public @interface IsPresent {

    String message() default "Файл порожній";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
