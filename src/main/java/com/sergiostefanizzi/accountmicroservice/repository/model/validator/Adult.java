package com.sergiostefanizzi.accountmicroservice.repository.model.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {AdultValidator.class})
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Adult {
    String message() default "is not valid! The user must be an adult";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
