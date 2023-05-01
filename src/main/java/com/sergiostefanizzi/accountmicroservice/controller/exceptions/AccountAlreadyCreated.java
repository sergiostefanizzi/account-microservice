package com.sergiostefanizzi.accountmicroservice.controller.exceptions;

public class AccountAlreadyCreated extends RuntimeException{
    public AccountAlreadyCreated(String message) {
        super(message);
    }
}
