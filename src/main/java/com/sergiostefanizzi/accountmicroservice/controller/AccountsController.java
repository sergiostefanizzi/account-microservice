package com.sergiostefanizzi.accountmicroservice.controller;

import com.sergiostefanizzi.accountmicroservice.api.AccountsApi;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.AccountPatch;
import com.sergiostefanizzi.accountmicroservice.service.AccountsService;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.ActionForbiddenException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


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

    @Override
    public ResponseEntity<Void> deleteAccountById(String accountId) {
        this.accountsService.remove(accountId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @Override
    public ResponseEntity<Void> activateAccountById(String accountId, String validationCode) {
        this.accountsService.active(accountId, validationCode);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Account> updateAccountById(String accountId, AccountPatch accountPatch) {
        Account updatedAccount = this.accountsService.update(accountId, accountPatch);
        return new ResponseEntity<>(updatedAccount, HttpStatus.OK);
    }


}
