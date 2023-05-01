package com.sergiostefanizzi.accountmicroservice.controller.exceptions;

public class AccountIdNotFound extends RuntimeException{
    public AccountIdNotFound(String message) {
        super(message);
    }
}
