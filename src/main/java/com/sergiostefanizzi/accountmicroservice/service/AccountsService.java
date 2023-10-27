package com.sergiostefanizzi.accountmicroservice.service;


import com.sergiostefanizzi.accountmicroservice.controller.converter.UserRepresentationToAccountConverter;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AccountAlreadyCreatedException;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AccountNotActivedException;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.AccountPatch;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class AccountsService {
    @Autowired
    private KeycloakService keycloakService;
    @Autowired
    private UserRepresentationToAccountConverter userRepresentationToAccountConverter;

    @Transactional
    public Account save(Account newAccount) {
        if(!keycloakService.getUsersByEmail(newAccount.getEmail()).isEmpty()){
            throw new AccountAlreadyCreatedException(newAccount.getEmail());
        }

        return userRepresentationToAccountConverter.convert(
                keycloakService.createUser(newAccount)
        );
    }


    @Transactional
    public void remove(String accountId){
        keycloakService.removeUser(accountId);
    }

    @Transactional
    public void active(String accountId, String validationCode){
        if(!keycloakService.validateEmail(accountId, validationCode)){
            throw new AccountNotActivedException(String.valueOf(accountId));
        }
    }

    @Transactional
    public Account update(String accountId, AccountPatch accountToUpdate){
        return userRepresentationToAccountConverter.convert(
                keycloakService.updateUser(accountId, accountToUpdate));
    }


}
