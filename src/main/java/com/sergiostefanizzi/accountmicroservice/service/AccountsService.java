package com.sergiostefanizzi.accountmicroservice.service;

import com.sergiostefanizzi.accountmicroservice.controller.converter.AccountsToJpaConverter;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AccountAlreadyCreatedException;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AccountNotFoundException;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.ValidationCodeNotValidException;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.AccountPatch;
import com.sergiostefanizzi.accountmicroservice.repository.AccountsRepository;
import com.sergiostefanizzi.accountmicroservice.repository.model.AccountJpa;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
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
        UUID validationCode = UUID.randomUUID();
        AccountJpa newAccountJpa = this.accountsToJpaConverter.convert(newAccount);
        Objects.requireNonNull(newAccountJpa).setValidationCode(validationCode.toString());
        log.info("validation code ---> "+validationCode);
        AccountJpa savedAccountJpa = this.accountsRepository.save(Objects.requireNonNull(newAccountJpa));
        return this.accountsToJpaConverter.convertBack(savedAccountJpa);
    }

    @Transactional
    public void remove(Long accountId){
        //Spring non riesce a capire il metodo
        if(accountId == null){
            throw new AccountNotFoundException("Bad request! Id is not valid");
        }

        this.accountsRepository.findById(accountId).orElseThrow(
                () -> new AccountNotFoundException(accountId)
        );
        this.accountsRepository.deleteById(accountId);
    }

    @Transactional
    public Account update(Long accountId, AccountPatch accountToUpdate){
        // TODO: controllo prima il campo
        return this.accountsToJpaConverter.convertBack(this.accountsRepository.findById(accountId)
                .map(accountJpa -> {
                    accountJpa.setName(accountToUpdate.getName() == null ? accountJpa.getName() : accountToUpdate.getName());
                    accountJpa.setSurname(accountToUpdate.getSurname() == null ? accountJpa.getSurname() : accountToUpdate.getSurname());
                    accountJpa.setGender(accountToUpdate.getGender() == null ? accountJpa.getGender():  AccountJpa.Gender.valueOf(accountToUpdate.getGender().toString()));
                    accountJpa.setPassword(accountToUpdate.getPassword() == null ? accountJpa.getPassword() : accountToUpdate.getPassword());
                    accountJpa.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
                    return this.accountsRepository.save(accountJpa);
                })
                .orElseThrow(() -> new AccountNotFoundException(accountId)));
    }

    @Transactional
    public void active(Long accountId, String validationCode){
        Optional<AccountJpa> optionalAccountJpa = this.accountsRepository.findById(accountId);

        if (optionalAccountJpa.isPresent()){
            AccountJpa accountJpa = optionalAccountJpa.get();
            if (accountJpa.getValidationCode().equals(validationCode)){
                if (accountJpa.getValidatedAt() == null){
                    accountJpa.setValidatedAt(Timestamp.valueOf(LocalDateTime.now()));
                    this.accountsRepository.save(accountJpa);
                }

            }else {
                throw new ValidationCodeNotValidException(validationCode);
            }
        }else{
            throw  new AccountNotFoundException(accountId);
        }

    }



}
