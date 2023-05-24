package com.sergiostefanizzi.accountmicroservice.service;

import com.sergiostefanizzi.accountmicroservice.controller.converter.AccountToJpaConverter;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AccountAlreadyCreatedException;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AccountNotActivedException;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AccountNotFoundException;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.AccountPatch;
import com.sergiostefanizzi.accountmicroservice.repository.AccountsRepository;
import com.sergiostefanizzi.accountmicroservice.repository.model.AccountJpa;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    private AccountToJpaConverter accountToJpaConverter;

    @Transactional
    public Account save(Account newAccount) {

        if(this.accountsRepository.findByEmail(newAccount.getEmail()).isPresent()){
            throw new AccountAlreadyCreatedException(newAccount.getEmail());
        }
        UUID validationCode = UUID.randomUUID();
        AccountJpa newAccountJpa = this.accountToJpaConverter.convert(newAccount);
        Objects.requireNonNull(newAccountJpa).setValidationCode(validationCode.toString());
        log.info("validation code ---> "+validationCode);
        AccountJpa savedAccountJpa = this.accountsRepository.save(Objects.requireNonNull(newAccountJpa));
        return this.accountToJpaConverter.convertBack(savedAccountJpa);
    }

    @Transactional
    public void remove(Long accountId){
        //Spring non riesce a capire il metodo
        if(accountId == null){
            throw new AccountNotFoundException("Bad request! Id is not valid");
        }

        AccountJpa accountToRemove = this.accountsRepository.findById(accountId)
                .filter(accountJpa -> accountJpa.getDeletedAt() == null)
                .orElseThrow(
                () -> new AccountNotFoundException(accountId)
        );
        accountToRemove.setDeletedAt(Timestamp.valueOf(LocalDateTime.now()));
        this.accountsRepository.save(accountToRemove);

    }

    @Transactional
    public Account update(Long accountId, AccountPatch accountToUpdate){
        AccountJpa savedAccountJpa = this.accountsRepository.findById(accountId)
                .filter(accountJpa -> accountJpa.getDeletedAt() == null) //non posso aggiornare un account eliminato
                .orElseThrow(()-> new AccountNotFoundException(accountId));
        // check perche' modifico solo i campi passati dalla patch
        if (StringUtils.hasText(accountToUpdate.getName()))  savedAccountJpa.setName(accountToUpdate.getName());
        if (StringUtils.hasText(accountToUpdate.getSurname()))  savedAccountJpa.setSurname(accountToUpdate.getSurname());
        if (StringUtils.hasText(accountToUpdate.getGender().toString()))  savedAccountJpa.setGender(AccountJpa.Gender.valueOf(accountToUpdate.getGender().toString()));
        if (StringUtils.hasText(accountToUpdate.getPassword()))  savedAccountJpa.setPassword( accountToUpdate.getPassword());
        savedAccountJpa.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        AccountJpa updatedAccountJpa = this.accountsRepository.save(savedAccountJpa);
        return this.accountToJpaConverter.convertBack(updatedAccountJpa);

    }

    @Transactional
    public void active(Long accountId, String validationCode){
        AccountJpa accountJpaToActive = this.accountsRepository.findById(accountId)
                .filter(accountJpa -> accountJpa.getValidationCode().equals(validationCode))
                .orElseThrow(()-> new AccountNotActivedException(accountId));

        if (accountJpaToActive.getValidatedAt() == null) {
            accountJpaToActive.setValidatedAt(Timestamp.valueOf(LocalDateTime.now()));
            this.accountsRepository.save(accountJpaToActive);
        }

    }

}
