package com.sergiostefanizzi.accountmicroservice.controller.exceptions;

public class ValidationCodeNotValidException extends RuntimeException {
    public ValidationCodeNotValidException(String validationCode) {
        super(validationCode);
    }
}
