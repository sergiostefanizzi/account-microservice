package com.sergiostefanizzi.accountmicroservice.system.exceptions;

public class ActionForbiddenException extends RuntimeException{
    public ActionForbiddenException(String message) {
        super(message);
    }
}
