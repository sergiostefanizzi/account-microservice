package com.sergiostefanizzi.accountmicroservice.service;

import com.sergiostefanizzi.accountmicroservice.controller.converter.UserRepresentationToAccountConverter;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AccountNotFoundException;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AdminAlreadyCreatedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminsService {
    @Autowired
    private KeycloakService keycloakService;
    @Autowired
    private UserRepresentationToAccountConverter userRepresentationToAccountConverter;

    @Transactional
    public String save(String accountId) {
        return keycloakService.createAdmin(accountId)
                .orElseThrow(() -> new AdminAlreadyCreatedException(accountId));
    }

    @Transactional
    public void remove(String accountId) {
         keycloakService.blockUser(accountId)
                 .orElseThrow(() -> new AccountNotFoundException(accountId));

    }

    @Transactional
    public List<Account> findAll(Boolean removedAccount) {
        if(!removedAccount){
            return this.keycloakService.findAllActive(false).stream().map(this.userRepresentationToAccountConverter::convert).toList();
        }else{
            return this.keycloakService.findAllActive(true).stream().map(this.userRepresentationToAccountConverter::convert).toList();
        }
    }

}
