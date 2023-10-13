package com.sergiostefanizzi.accountmicroservice.system.exceptions;

public class AccountAlreadyActivatedException extends RuntimeException{
    public AccountAlreadyActivatedException(String message) {
        super(message);
    }

    public AccountAlreadyActivatedException(Long accountId) {
        super(accountId.toString());
    }
}
