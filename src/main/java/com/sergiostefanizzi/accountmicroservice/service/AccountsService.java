package com.sergiostefanizzi.accountmicroservice.service;

import com.sergiostefanizzi.accountmicroservice.controller.converter.AccountsToJpaConverter;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AccountAlreadyCreatedException;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AccountIdNotFoundException;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AccountNotFoundException;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.AccountPatch;
import com.sergiostefanizzi.accountmicroservice.repository.AccountsRepository;
import com.sergiostefanizzi.accountmicroservice.repository.model.AccountJpa;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class AccountsService {
    @Autowired
    private AccountsRepository accountsRepository;
    @Autowired
    private AccountsToJpaConverter accountsToJpaConverter;

    @Transactional
    public Account save(Account newAccount) {

        if(this.accountsRepository.findByEmail(newAccount.getEmail()).isPresent()){
            throw new AccountAlreadyCreatedException(newAccount.getEmail());
        }
        AccountJpa newAccountJpa = this.accountsToJpaConverter.convert(newAccount);
        AccountJpa savedAccountJpa = this.accountsRepository.save(Objects.requireNonNull(newAccountJpa));
        return this.accountsToJpaConverter.convertBack(savedAccountJpa);
    }

    @Transactional
    public void remove(Long accountId){
        //Spring non riesce a capire il metodo
        if(accountId == null){
            throw new AccountIdNotFoundException("Bad request! Id is not valid");
        }

        this.accountsRepository.findById(accountId).orElseThrow(
                () -> new AccountIdNotFoundException("Bad request! Id is not valid")
        );
        this.accountsRepository.deleteById(accountId);
    }

    @Transactional
    public Account update(Long accountId, AccountPatch accountToUpdate){
        return this.accountsToJpaConverter.convertBack(this.accountsRepository.findById(accountId)
                .map(accountJpa -> {
                    accountJpa.setName(accountToUpdate.getName() == null ? accountJpa.getName() : accountToUpdate.getName());
                    accountJpa.setSurname(accountToUpdate.getSurname() == null ? accountJpa.getSurname() : accountToUpdate.getSurname());
                    accountJpa.setGender(accountToUpdate.getGender() == null ? accountJpa.getGender():  AccountJpa.Gender.valueOf(accountToUpdate.getGender().toString()));
                    accountJpa.setPassword(accountToUpdate.getPassword() == null ? accountJpa.getPassword() : accountToUpdate.getPassword());
                    return this.accountsRepository.save(accountJpa);
                })
                .orElseThrow(() -> new AccountNotFoundException("Account not found!")));
    }



}
