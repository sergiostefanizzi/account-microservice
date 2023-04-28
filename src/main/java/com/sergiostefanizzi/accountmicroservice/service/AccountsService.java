package com.sergiostefanizzi.accountmicroservice.service;

import com.sergiostefanizzi.accountmicroservice.controller.converter.AccountsToJpaConverter;
import com.sergiostefanizzi.accountmicroservice.exceptions.AccountAlreadyCreatedException;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.repository.AccountsRepository;
import com.sergiostefanizzi.accountmicroservice.repository.model.AccountJpa;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.empty;

@Service
public class AccountsService {
    @Autowired
    private AccountsRepository accountsRepository;
    @Autowired
    private AccountsToJpaConverter accountsToJpaConverter;

    @Transactional
    public Optional<Account> save(Account newAccount){
        AccountJpa newAccountJpa = this.accountsToJpaConverter.convert(newAccount);
        if(this.accountsRepository.findbyEmail(newAccount.getEmail()).isEmpty()){
            AccountJpa savedAccountJpa = this.accountsRepository.save(newAccountJpa);
            return Optional.ofNullable(this.accountsToJpaConverter.convertBack(savedAccountJpa));
        }

        return null;

    }
}
