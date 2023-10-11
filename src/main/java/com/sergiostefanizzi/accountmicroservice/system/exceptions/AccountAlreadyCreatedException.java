package com.sergiostefanizzi.accountmicroservice.system.exceptions;

public class AccountAlreadyCreatedException extends RuntimeException{
    public AccountAlreadyCreatedException(String message) {
        super(message);
    }
}
