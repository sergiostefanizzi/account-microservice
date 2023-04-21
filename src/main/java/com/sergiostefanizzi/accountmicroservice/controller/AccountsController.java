package com.sergiostefanizzi.accountmicroservice.controller;

import com.sergiostefanizzi.accountmicroservice.api.AccountsApi;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.UpdateAccountByIdRequest;
import com.sergiostefanizzi.accountmicroservice.service.AccountsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Optional;

@Controller
public class AccountsController implements AccountsApi {
    private AccountsService accountsService;

    @Autowired
    public void setAccountsService(AccountsService accountsService) {
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
    public ResponseEntity<Account> addAccount(Account account) {
        return AccountsApi.super.addAccount(account);
    }

    @Override
    public ResponseEntity<Void> deleteAccountById(Long accountId) {
        return AccountsApi.super.deleteAccountById(accountId);
    }

    @Override
    public ResponseEntity<Account> updateAccountById(Long accountId, UpdateAccountByIdRequest updateAccountByIdRequest) {
        return AccountsApi.super.updateAccountById(accountId, updateAccountByIdRequest);
    }
}
