package com.sergiostefanizzi.accountmicroservice.controller;

import com.sergiostefanizzi.accountmicroservice.api.AccountsApi;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.AccountPatch;
import com.sergiostefanizzi.accountmicroservice.service.AccountsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@Slf4j
public class AccountsController implements AccountsApi {
    private final AccountsService accountsService;

    @Override
    public ResponseEntity<Account> addAccount(Account newAccount){
        Account savedAccount = this.accountsService.save(newAccount);
        return new ResponseEntity<>(savedAccount, HttpStatus.CREATED);
    }
    /*
    @Override
    public ResponseEntity<Void> activateAccountById(Long accountId, String validationCode) {
        //TODO: fare controllo di permesso
        this.accountsService.active(accountId, validationCode);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @Override
    public ResponseEntity<Void> deleteAccountById(Long accountId) {
        //TODO: fare controllo di permesso
        this.accountsService.remove(accountId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Account> updateAccountById(Long accountId, AccountPatch updateAccountByIdRequest) {
        //TODO: fare controllo di permesso
        Account updatedAccount = this.accountsService.update(accountId, updateAccountByIdRequest);
        return new ResponseEntity<>(updatedAccount, HttpStatus.OK);
    }

     */

}
