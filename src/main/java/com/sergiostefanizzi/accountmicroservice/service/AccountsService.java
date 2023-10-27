package com.sergiostefanizzi.accountmicroservice.service;

import com.sergiostefanizzi.accountmicroservice.controller.converter.AccountToJpaConverter;
import com.sergiostefanizzi.accountmicroservice.controller.converter.UserRepresentationToAccountConverter;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AccountAlreadyCreatedException;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AccountNotActivedException;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.AccountPatch;
import com.sergiostefanizzi.accountmicroservice.repository.AccountsRepository;
import com.sergiostefanizzi.accountmicroservice.model.AccountJpa;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountsService {

    @Autowired
    private KeycloakService keycloakService;
    @Autowired
    private UserRepresentationToAccountConverter userRepresentationToAccountConverter;

    private final AccountsRepository accountsRepository;

    private final AccountToJpaConverter accountToJpaConverter;


    @Transactional
    public Account save(Account newAccount) {
        if(!keycloakService.getUsersByEmail(newAccount.getEmail()).isEmpty()){
            throw new AccountAlreadyCreatedException(newAccount.getEmail());
        }

        return userRepresentationToAccountConverter.convert(
                keycloakService.createUser(newAccount)
        );
    }


    /*
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
        if (StringUtils.hasText(accountToUpdate.getGender().toString()))  savedAccountJpa.setGender(Account.GenderEnum.valueOf(accountToUpdate.getGender().toString()));
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
     */

}
