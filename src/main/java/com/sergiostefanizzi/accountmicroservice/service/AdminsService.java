package com.sergiostefanizzi.accountmicroservice.service;

import com.sergiostefanizzi.accountmicroservice.controller.converter.AccountToJpaConverter;
import com.sergiostefanizzi.accountmicroservice.controller.converter.AccountJpaToAdminConverter;

import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AccountNotFoundException;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AdminAlreadyCreatedException;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.Admin;
import com.sergiostefanizzi.accountmicroservice.repository.AccountsRepository;
import com.sergiostefanizzi.accountmicroservice.repository.model.AccountJpa;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AdminsService {
    @Autowired
    private AccountsRepository accountsRepository;
    @Autowired
    private AccountToJpaConverter accountToJpaConverter;
    @Autowired
    private AccountJpaToAdminConverter accountJpaToAdminConverter;

    @Transactional
    public List<Account> findAll(Boolean removedAccount) {

        return this.accountsRepository.findAll()
                .stream()
                .filter(accountJpa ->
                        (accountJpa.getDeletedAt() == null && !removedAccount)
                || (accountJpa.getDeletedAt() != null && removedAccount))
                .map(this.accountToJpaConverter::convertBack)
                .collect(Collectors.toList());
    }
    @Transactional
    public Admin save(Long accountId) {
        //controllo esistenza account
        AccountJpa adminToBe = this.accountsRepository.findById(accountId)
                .filter(accountJpa -> accountJpa.getDeletedAt() == null)
                .orElseThrow(
                () -> new AccountNotFoundException(accountId)
        );
        //controllo che l'account non sia gia' un admin
        if (adminToBe.getAdmin()){
            throw new AdminAlreadyCreatedException(accountId);
        }
        adminToBe.setAdmin(true);
        //salvo l'account come admin
        return this.accountJpaToAdminConverter.convert(
                this.accountsRepository.save(adminToBe)
        );
    }

    @Transactional
    public void remove(Long accountId) {
        //controllo esistenza account
        AccountJpa accountToDelete = this.accountsRepository.findById(accountId)
                .filter(accountJpa -> accountJpa.getDeletedAt() == null)
                .orElseThrow(
                        () -> new AccountNotFoundException(accountId)
                );
        Timestamp deletedAt = Timestamp.valueOf(LocalDateTime.now());
        accountToDelete.setDeletedAt(deletedAt);
        this.accountsRepository.save(accountToDelete);
    }


}
