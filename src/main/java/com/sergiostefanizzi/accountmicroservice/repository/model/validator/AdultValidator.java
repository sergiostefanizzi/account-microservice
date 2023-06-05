package com.sergiostefanizzi.accountmicroservice.repository.model.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class AdultValidator implements ConstraintValidator<Adult, LocalDate> {
    @Override
    public void initialize(Adult constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(LocalDate birthdate, ConstraintValidatorContext context) {
        // ho inserito questo controllo per evitare il 500 quando manca il campo birthdate
        if (birthdate == null){
            return true;
        }
        Period period = Period.between(birthdate, LocalDate.now());

        // se la data e' nel futuro, la differenza tra e' degativa, quindi ritorno true, per non mostrare l'errore della maggiore età
        return period.getYears() >= 18;
    }
}