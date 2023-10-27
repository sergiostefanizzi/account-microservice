package com.sergiostefanizzi.accountmicroservice.service;

import com.sergiostefanizzi.accountmicroservice.controller.converter.AccountToJpaConverter;
import com.sergiostefanizzi.accountmicroservice.controller.converter.AccountJpaToAdminConverter;

import com.sergiostefanizzi.accountmicroservice.system.exceptions.AdminAlreadyCreatedException;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.Admin;
import com.sergiostefanizzi.accountmicroservice.repository.AccountsRepository;
import com.sergiostefanizzi.accountmicroservice.model.AccountJpa;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminsService {

    private final AccountsRepository accountsRepository;

    private final AccountToJpaConverter accountToJpaConverter;

    private final AccountJpaToAdminConverter accountJpaToAdminConverter;
    /*

    @Transactional
    public List<Account> findAll(Boolean removedAccount) {

        if(!removedAccount){
            return this.accountsRepository.findAllActive().stream().map(this.accountToJpaConverter::convertBack).toList();
        }else{
            return this.accountsRepository.findAllActiveAndDeleted().stream().map(this.accountToJpaConverter::convertBack).toList();
        }
    }

    @Transactional
    public Admin save(Long accountId) {

        AccountJpa adminToBe = this.accountsRepository.getReferenceById(accountId);
        //controllo che l'account non sia gia' un admin
        if (adminToBe.getIsAdmin()){
            throw new AdminAlreadyCreatedException(accountId);
        }
        adminToBe.setIsAdmin(true);

        return this.accountJpaToAdminConverter.convert(
                this.accountsRepository.save(adminToBe)
        );
    }

    @Transactional
    public void remove(Long accountId) {
        AccountJpa accountToDelete = this.accountsRepository.getReferenceById(accountId);
        accountToDelete.setDeletedAt(LocalDateTime.now());
        this.accountsRepository.save(accountToDelete);
    }

     */


}
