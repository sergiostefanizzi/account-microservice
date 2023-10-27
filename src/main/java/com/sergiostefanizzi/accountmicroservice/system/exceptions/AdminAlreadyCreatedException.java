package com.sergiostefanizzi.accountmicroservice.system.exceptions;

public class AdminAlreadyCreatedException extends RuntimeException{
    public AdminAlreadyCreatedException(String message) {
        super(message);
    }

}
