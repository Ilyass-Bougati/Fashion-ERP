package com.sefault.server.annotation;

import com.sefault.server.validator.PhoneNumberValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PhoneNumber {
    // The default error message if validation fails
    String message() default "Invalid phone number. Must be exactly 10 digits.";

    // Boilerplate required by the validation API
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
