package com.sergiostefanizzi.accountmicroservice.service;

import com.sergiostefanizzi.accountmicroservice.controller.converter.AccountsToJpaConverter;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AccountAlreadyCreated;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AccountIdNotFound;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.repository.AccountsRepository;
import com.sergiostefanizzi.accountmicroservice.repository.model.AccountJpa;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountsService {
    @Autowired
    private AccountsRepository accountsRepository;
    @Autowired
    private AccountsToJpaConverter accountsToJpaConverter;

    @Transactional
    public Optional<Account> save(Account newAccount) {

        if(this.accountsRepository.findByEmail(newAccount.getEmail()).isPresent()){
            throw new AccountAlreadyCreated(newAccount.getEmail());
        }
        AccountJpa newAccountJpa = this.accountsToJpaConverter.convert(newAccount);
        AccountJpa savedAccountJpa = this.accountsRepository.save(newAccountJpa);
        return Optional.ofNullable(this.accountsToJpaConverter.convertBack(savedAccountJpa));
    }

    @Transactional
    public void remove(Long accountId){
        this.accountsRepository.findById(accountId).orElseThrow(
                () -> new AccountIdNotFound("Account not found")
        );
        this.accountsRepository.deleteById(accountId);
    }


}
