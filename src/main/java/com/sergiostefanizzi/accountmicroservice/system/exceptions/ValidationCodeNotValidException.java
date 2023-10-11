package com.sergiostefanizzi.accountmicroservice.system.exceptions;

public class ValidationCodeNotValidException extends RuntimeException {
    public ValidationCodeNotValidException(String validationCode) {
        super(validationCode);
    }
}
