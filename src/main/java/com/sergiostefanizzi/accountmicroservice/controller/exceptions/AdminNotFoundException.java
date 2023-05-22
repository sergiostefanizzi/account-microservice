package com.sergiostefanizzi.accountmicroservice.controller.exceptions;

public class AdminNotFoundException extends RuntimeException{
    public AdminNotFoundException(String message) {
        super(message);
    }

    public AdminNotFoundException(Long accountId) {
        super(accountId.toString());
    }
}
