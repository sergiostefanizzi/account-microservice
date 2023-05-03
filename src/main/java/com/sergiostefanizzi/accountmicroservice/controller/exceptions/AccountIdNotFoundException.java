package com.sergiostefanizzi.accountmicroservice.controller.exceptions;

public class AccountIdNotFoundException extends RuntimeException{
    public AccountIdNotFoundException(String message) {
        super(message);
    }
}
