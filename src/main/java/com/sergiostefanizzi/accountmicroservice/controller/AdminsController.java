package com.sergiostefanizzi.accountmicroservice.controller;

import com.sergiostefanizzi.accountmicroservice.api.AdminsApi;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.Admin;
import com.sergiostefanizzi.accountmicroservice.service.AdminsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;
import java.util.Optional;

@Controller
public class AdminsController implements AdminsApi {
    private final AdminsService adminsService;

    @Autowired
    public AdminsController(AdminsService adminsService) {
        this.adminsService = adminsService;
    }



    @Override
    public Optional<NativeWebRequest> getRequest() {
        return AdminsApi.super.getRequest();
    }


    @Override
    public ResponseEntity<Admin> addAdminById(Long accountId) {
        Admin savedAdmin = this.adminsService.save(accountId);
        return new ResponseEntity<>(savedAdmin, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteAdminById(Long accountId) {
        this.adminsService.remove(accountId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<List<Account>> findAllAccounts(Boolean removedAccount) {
        List<Account> accountList = this.adminsService.findAll(removedAccount);
        return new ResponseEntity<>(accountList, HttpStatus.OK);
    }
}
