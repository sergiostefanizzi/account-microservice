package com.sergiostefanizzi.accountmicroservice.service;

import com.sergiostefanizzi.accountmicroservice.controller.converter.AccountToJpaConverter;

import com.sergiostefanizzi.accountmicroservice.system.exceptions.AdminAlreadyCreatedException;
import com.sergiostefanizzi.accountmicroservice.model.Admin;
import com.sergiostefanizzi.accountmicroservice.repository.AccountsRepository;
import com.sergiostefanizzi.accountmicroservice.model.AccountJpa;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminsService {

    private final AccountsRepository accountsRepository;

    private final AccountToJpaConverter accountToJpaConverter;

    @Autowired
    private KeycloakService keycloakService;

    @Transactional
    public String save(String accountId) {
        return keycloakService.createAdmin(accountId)
                .orElseThrow(() -> new AdminAlreadyCreatedException(accountId));
    }

    @Transactional
    public void remove(String accountId) {
        keycloakService.blockUser(accountId);
    }
    /*

    @Transactional
    public List<Account> findAll(Boolean removedAccount) {

        if(!removedAccount){
            return this.accountsRepository.findAllActive().stream().map(this.accountToJpaConverter::convertBack).toList();
        }else{
            return this.accountsRepository.findAllActiveAndDeleted().stream().map(this.accountToJpaConverter::convertBack).toList();
        }
    }





     */


}
