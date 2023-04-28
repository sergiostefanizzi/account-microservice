package com.sergiostefanizzi.accountmicroservice.exceptions;

public class AccountAlreadyCreatedException extends RuntimeException{
    public AccountAlreadyCreatedException(String email) {
        super("Email "+email+" already exists");
    }
}
