package com.sergiostefanizzi.accountmicroservice.controller;

import com.sergiostefanizzi.accountmicroservice.api.AdminsApi;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.Admin;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;
import java.util.Optional;

@Controller
public class AdminsController implements AdminsApi {
    @Override
    public Optional<NativeWebRequest> getRequest() {
        return AdminsApi.super.getRequest();
    }

    @Override
    public ResponseEntity<Admin> addAdminById(Long accountId) {
        return AdminsApi.super.addAdminById(accountId);
    }

    @Override
    public ResponseEntity<Void> deleteAdminById(Long accountId) {
        return AdminsApi.super.deleteAdminById(accountId);
    }

    @Override
    public ResponseEntity<List<Account>> findAllAccounts(Boolean removedAccount) {
        return AdminsApi.super.findAllAccounts(removedAccount);
    }
}
