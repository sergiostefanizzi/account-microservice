package com.sergiostefanizzi.accountmicroservice.controller;

import com.sergiostefanizzi.accountmicroservice.api.AdminsApi;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.Admin;
import com.sergiostefanizzi.accountmicroservice.service.AdminsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;


@RestController
@RequiredArgsConstructor
@Slf4j
public class AdminsController implements AdminsApi {
    private final AdminsService adminsService;

    @Override
    public ResponseEntity<String> addAdminById(String accountId) {
        String adminId = this.adminsService.save(accountId);
        return new ResponseEntity<>(adminId, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteAdminById(String accountId) {
        this.adminsService.remove(accountId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    /*
    @Override
    public ResponseEntity<List<Account>> findAllAccounts(Boolean removedAccount) {
        List<Account> accountList = this.adminsService.findAll(removedAccount);
        return new ResponseEntity<>(accountList, HttpStatus.OK);
    }

     */
}
