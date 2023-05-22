package com.sergiostefanizzi.accountmicroservice.service;

import com.sergiostefanizzi.accountmicroservice.controller.converter.AccountToJpaConverter;
import com.sergiostefanizzi.accountmicroservice.controller.converter.AdminJpaToAdminConverter;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AccountNotFoundException;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AdminAlreadyCreatedException;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AdminNotFoundException;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.Admin;
import com.sergiostefanizzi.accountmicroservice.repository.AccountsRepository;
import com.sergiostefanizzi.accountmicroservice.repository.AdminsRepository;
import com.sergiostefanizzi.accountmicroservice.repository.model.AccountJpa;
import com.sergiostefanizzi.accountmicroservice.repository.model.AdminJpa;
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
    private AdminsRepository adminsRepository;
    @Autowired
    private AccountsRepository accountsRepository;
    @Autowired
    private AccountToJpaConverter accountToJpaConverter;
    @Autowired
    private AdminJpaToAdminConverter adminsToJpaConverter;

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

    public Admin save(Long accountId) {
        //controllo esistenza account
        AccountJpa adminToBe = this.accountsRepository.findById(accountId)
                .filter(accountJpa -> accountJpa.getDeletedAt() == null)
                .orElseThrow(
                () -> new AccountNotFoundException(accountId)
        );
        //controllo che l'account non sia gia' un admin
        if (this.adminsRepository.findById(accountId).isPresent()){
            throw new AdminAlreadyCreatedException(accountId);
        }
        //salvo l'account come admin
        return this.adminsToJpaConverter.convert(
                this.adminsRepository.save(
                new AdminJpa(Timestamp.valueOf(LocalDateTime.now()),adminToBe)
        ));
    }

    public void remove(Long accountId) {
        //controllo esistenza account
        this.accountsRepository.findById(accountId)
                .filter(accountJpa -> accountJpa.getDeletedAt() == null)
                .orElseThrow(
                        () -> new AccountNotFoundException(accountId)
                );
        //controllo esistenza admin
        this.adminsRepository.findById(accountId)
                .orElseThrow(
                        () -> new AdminNotFoundException(accountId)
                );
        this.adminsRepository.deleteById(accountId);
    }
}
