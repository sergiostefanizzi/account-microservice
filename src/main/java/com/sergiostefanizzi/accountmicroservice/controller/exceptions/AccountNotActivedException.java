package com.sergiostefanizzi.accountmicroservice.controller.exceptions;

public class AccountNotActivedException extends RuntimeException{
    public AccountNotActivedException(String message) {
        super(message);
    }

    public AccountNotActivedException(Long accountId) {
        super(accountId.toString());
    }
}
