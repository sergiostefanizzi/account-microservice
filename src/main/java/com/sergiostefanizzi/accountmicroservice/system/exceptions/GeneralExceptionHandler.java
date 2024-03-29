package com.sergiostefanizzi.accountmicroservice.system.exceptions;

import com.sergiostefanizzi.accountmicroservice.system.exceptions.*;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GeneralExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ActionForbiddenException.class)
    public ResponseEntity<Object> handleActionForbiddenException(ActionForbiddenException ex, WebRequest request){
        log.error(ex.getMessage(),ex);
        String error = "Forbidden: Account with id "+ex.getMessage()+" can not perform this action";
        Map<String, String> body = new HashMap<>();
        body.put("error", error);
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request){
        log.error(ex.getMessage(),ex);
        String error = "Forbidden!";
        Map<String, String> body = new HashMap<>();
        body.put("error", error);
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<Object> handleNumberFormatException(NumberFormatException ex, WebRequest request){
        log.error(ex.getMessage(),ex);
        String error = "ID is not valid!";
        Map<String, String> body = new HashMap<>();
        body.put("error", error);
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    //post
    @ExceptionHandler(AccountAlreadyCreatedException.class)
    public ResponseEntity<Object> handleAccountAlreadyCreatedException(AccountAlreadyCreatedException ex, WebRequest request){
        log.error(ex.getMessage(),ex);
        String error = "Conflict! Account with email "+ex.getMessage()+" already created!";
        Map<String, String> body = new HashMap<>();
        body.put("error", error);
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(AccountAlreadyActivatedException.class)
    public ResponseEntity<Object> handleAccountAlreadyActivatedException(AccountAlreadyActivatedException ex, WebRequest request){
        log.error(ex.getMessage(),ex);
        String error = "Account with id "+ex.getMessage()+" already activated!";
        Map<String, String> body = new HashMap<>();
        body.put("error", error);
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    //update
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Object> handleAccountNotFoundException(AccountNotFoundException ex, WebRequest request){
        log.error(ex.getMessage(),ex);
        String error = "Account with id "+ex.getMessage()+" not found!";
        Map<String, String> body = new HashMap<>();
        body.put("error", error);
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }




    @ExceptionHandler({RuntimeException.class, Exception.class})
    public ResponseEntity<Object> handleNotManagedException(RuntimeException ex, WebRequest request){
        log.error(ex.getMessage(),ex);
        String error = "Internal Error Server!";
        Map<String, String> body = new HashMap<>();
        body.put("error", error);
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(EmailNotValidatedException.class)
    public ResponseEntity<Object> handleEmailNotValidatedException(EmailNotValidatedException ex, WebRequest request){
        log.error(ex.getMessage(),ex);
        String error = "Account's email with id "+ex.getMessage()+" is not validated";
        Map<String, String> body = new HashMap<>();
        body.put("error", error);
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(AccountNotActivedException.class)
    public ResponseEntity<Object> handleAccountNotActivedException(AccountNotActivedException ex, WebRequest request){
        log.error(ex.getMessage(),ex);
        String error = "Error during activation of the account!";
        Map<String, String> body = new HashMap<>();
        body.put("error", error);
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handelConstraintViolationException(ConstraintViolationException ex, WebRequest request){
        log.error(ex.getMessage(),ex);
        String error = ex.getMessage();
        Map<String, String> body = new HashMap<>();
        body.put("error", error);
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }


    @ExceptionHandler(AdminAlreadyCreatedException.class)
    public ResponseEntity<Object> handleAdminAlreadyCreatedException(AdminAlreadyCreatedException ex, WebRequest request){
        log.error(ex.getMessage(),ex);
        String error = "Conflict! Admin with id "+ex.getMessage()+" already created!";
        Map<String, String> body = new HashMap<>();
        body.put("error", error);
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.error(ex.getMessage(),ex);
        String error = "Required request parameter is missing";
        Map<String, String> body = new HashMap<>();
        body.put("error", error);
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.error(ex.getMessage(),ex);
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField()+" "+error.getDefaultMessage())
                .collect(Collectors.toList());
        Map<String, Object> body = new HashMap<>();
        body.put("error", errors);
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }


    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.error(ex.getMessage(),ex);
        String error = "Type mismatch";
        Map<String, String> body = new HashMap<>();
        body.put("error", error);
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }


    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.error(ex.getMessage(),ex);
        String error = "Message is not readable";
        Map<String, String> body = new HashMap<>();
        body.put("error", error);
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }


    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.error(ex.getMessage(),ex);
        String error = "Message is not writable";
        Map<String, String> body = new HashMap<>();
        body.put("error", error);
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

}
