package com.sergiostefanizzi.accountmicroservice.system.exceptions;

public class EmailNotValidatedException extends RuntimeException{
    public EmailNotValidatedException(String message) {
        super(message);
    }
}
