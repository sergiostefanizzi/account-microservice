package com.sergiostefanizzi.accountmicroservice.controller.exceptions;

public class AdminAlreadyCreatedException extends RuntimeException{
    public AdminAlreadyCreatedException(String message) {
        super(message);
    }

    public AdminAlreadyCreatedException(Long accountId) {
        super(accountId.toString());
    }
}
