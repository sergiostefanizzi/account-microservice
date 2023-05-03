package com.sergiostefanizzi.accountmicroservice.controller.exceptions;

public class AccountAlreadyCreatedException extends RuntimeException{
    public AccountAlreadyCreatedException(String message) {
        super(message);
    }
}
