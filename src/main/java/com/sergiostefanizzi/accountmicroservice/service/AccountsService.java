package com.sergiostefanizzi.accountmicroservice.service;

import com.sergiostefanizzi.accountmicroservice.controller.converter.AccountToJpaConverter;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AccountAlreadyCreatedException;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AccountNotActivedException;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AccountNotFoundException;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.AccountPatch;
import com.sergiostefanizzi.accountmicroservice.repository.AccountsRepository;
import com.sergiostefanizzi.accountmicroservice.model.AccountJpa;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountsService {
    private final AccountsRepository accountsRepository;

    private final AccountToJpaConverter accountToJpaConverter;

    @Transactional
    public Account save(Account newAccount) {

        if(this.accountsRepository.findByEmail(newAccount.getEmail()).isPresent()){
            throw new AccountAlreadyCreatedException(newAccount.getEmail());
        }
        UUID validationCode = UUID.randomUUID();
        AccountJpa newAccountJpa = this.accountToJpaConverter.convert(newAccount);
        Objects.requireNonNull(newAccountJpa).setValidationCode(validationCode.toString());
        log.info("validation code ---> "+validationCode);
        return this.accountToJpaConverter.convertBack(
                this.accountsRepository.save(newAccountJpa)
        );
    }

    @Transactional
    public void remove(Long accountId){
        AccountJpa accountToRemove = this.accountsRepository.getReferenceById(accountId);
        accountToRemove.setDeletedAt(LocalDateTime.now());
        this.accountsRepository.save(accountToRemove);
    }

    @Transactional
    public Account update(Long accountId, AccountPatch accountToUpdate){
        AccountJpa savedAccountJpa = this.accountsRepository.getReferenceById(accountId);

        // check perche' modifico solo i campi passati dalla patch
        if (StringUtils.hasText(accountToUpdate.getName()))  savedAccountJpa.setName(accountToUpdate.getName());
        if (StringUtils.hasText(accountToUpdate.getSurname()))  savedAccountJpa.setSurname(accountToUpdate.getSurname());
        if (StringUtils.hasText(accountToUpdate.getGender().toString()))  savedAccountJpa.setGender(AccountJpa.Gender.valueOf(accountToUpdate.getGender().toString()));
        if (StringUtils.hasText(accountToUpdate.getPassword()))  savedAccountJpa.setPassword( accountToUpdate.getPassword());
        savedAccountJpa.setUpdatedAt(LocalDateTime.now());
        return this.accountToJpaConverter.convertBack(
                this.accountsRepository.save(savedAccountJpa)
        );
    }

    @Transactional
    public void active(Long accountId, String validationCode){
        AccountJpa accountJpaToActive = this.accountsRepository.getReferenceById(accountId);

        if(accountJpaToActive.getValidationCode().equals(validationCode)){
            accountJpaToActive.setValidatedAt(LocalDateTime.now());
            this.accountsRepository.save(accountJpaToActive);
        }else{
            throw new AccountNotActivedException(accountId);
        }
    }

}
