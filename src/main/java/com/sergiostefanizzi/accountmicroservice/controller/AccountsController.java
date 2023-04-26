package com.sergiostefanizzi.accountmicroservice.controller;

import com.sergiostefanizzi.accountmicroservice.api.AccountsApi;
import com.sergiostefanizzi.accountmicroservice.controller.converter.AccountJpaToAccountConverter;
import com.sergiostefanizzi.accountmicroservice.controller.converter.AccountToAccountJpaConverter;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.UpdateAccountByIdRequest;
import com.sergiostefanizzi.accountmicroservice.repository.model.AccountJpa;
import com.sergiostefanizzi.accountmicroservice.service.AccountsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Optional;

@Controller
public class AccountsController implements AccountsApi {
    private AccountsService accountsService;
    private AccountJpaToAccountConverter accountJpaToAccountConverter;
    private AccountToAccountJpaConverter accountToAccountJpaConverter;

    @Autowired
    public AccountsController(AccountsService accountsService, AccountJpaToAccountConverter accountJpaToAccountConverter, AccountToAccountJpaConverter accountToAccountJpaConverter) {
        this.accountsService = accountsService;
        this.accountJpaToAccountConverter = accountJpaToAccountConverter;
        this.accountToAccountJpaConverter = accountToAccountJpaConverter;
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
        AccountJpa newAccountJpa = this.accountToAccountJpaConverter.convert(account);
        AccountJpa savedAccountJpa = this.accountsService.save(newAccountJpa);
        Account savedAccount = this.accountJpaToAccountConverter.convert(savedAccountJpa);
        return new ResponseEntity<>(savedAccount, HttpStatus.CREATED);

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
