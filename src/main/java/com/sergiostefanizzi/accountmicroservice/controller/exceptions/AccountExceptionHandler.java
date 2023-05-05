package com.sergiostefanizzi.accountmicroservice.controller.exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class AccountExceptionHandler extends ResponseEntityExceptionHandler {

    //post
    @ExceptionHandler(AccountAlreadyCreatedException.class)
    public ResponseEntity<Object> handleAccountAlreadyCreatedException(AccountAlreadyCreatedException ex, WebRequest request){
        String error = "Conflict! Account with email "+ex.getMessage()+" already created!";
        Map<String, String> body = new HashMap<>();
        body.put("error", error);
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    //delete
    @ExceptionHandler(AccountIdNotFoundException.class)
    public ResponseEntity<Object> handleAccountIdNotFoundException(AccountIdNotFoundException ex, WebRequest request){
        String error = "Bad request! Id is not valid";
        Map<String, String> body = new HashMap<>();
        body.put("error", error);
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    //update
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Object> handleAccountNotFoundException(AccountNotFoundException ex, WebRequest request){
        String error = "Account not found!";
        Map<String, String> body = new HashMap<>();
        body.put("error", error);
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField()+" "+error.getDefaultMessage())
                .collect(Collectors.toList());
        Map<String, Object> body = new HashMap<>();
        body.put("errors", errors);
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler({RuntimeException.class, Exception.class})
    public ResponseEntity<Object> handleNotManagedException(RuntimeException ex, WebRequest request){
        String error = "Internal Error Server!";
        Map<String, String> body = new HashMap<>();
        body.put("error", error);
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }




}
