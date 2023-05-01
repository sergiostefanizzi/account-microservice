package com.sergiostefanizzi.accountmicroservice.controller;

import com.sergiostefanizzi.accountmicroservice.api.AccountsApi;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.UpdateAccountByIdRequest;
import com.sergiostefanizzi.accountmicroservice.service.AccountsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.context.request.NativeWebRequest;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class AccountsController implements AccountsApi {
    private AccountsService accountsService;

    @Autowired
    public AccountsController(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return AccountsApi.super.getRequest();
    }

    @Override
    public ResponseEntity<Void> activateAccountById(Long accountId, Long crc) {
        return AccountsApi.super.activateAccountById(accountId, crc);
    }

    @Override
    public ResponseEntity<Account> addAccount(Account newAccount){
        Optional<Account> savedAccount = this.accountsService.save(newAccount);
        return new ResponseEntity<>(savedAccount.get(), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteAccountById(Long accountId) {
        this.accountsService.remove(accountId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Account> updateAccountById(Long accountId, UpdateAccountByIdRequest updateAccountByIdRequest) {
        return AccountsApi.super.updateAccountById(accountId, updateAccountByIdRequest);
    }

}
