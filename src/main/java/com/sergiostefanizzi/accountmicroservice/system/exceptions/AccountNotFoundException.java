package com.sergiostefanizzi.accountmicroservice.system.exceptions;

public class AccountNotFoundException extends RuntimeException{
    public AccountNotFoundException(String message) {
        super(message);
    }

    public AccountNotFoundException(Long accountId) {
        super(accountId.toString());
    }
}
