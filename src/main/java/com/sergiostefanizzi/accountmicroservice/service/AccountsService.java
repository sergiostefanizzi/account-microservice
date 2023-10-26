package com.sergiostefanizzi.accountmicroservice.service;

import com.sergiostefanizzi.accountmicroservice.controller.converter.AccountToJpaConverter;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AccountAlreadyCreatedException;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AccountNotActivedException;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.AccountPatch;
import com.sergiostefanizzi.accountmicroservice.repository.AccountsRepository;
import com.sergiostefanizzi.accountmicroservice.model.AccountJpa;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountsService {

    private static final String REALM_NAME = "social-accounts";
    private final AccountsRepository accountsRepository;

    private final AccountToJpaConverter accountToJpaConverter;

     @Autowired
     private Keycloak keycloak;
    /*
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

     */

    @Transactional
    public Account save(Account newAccount) {
        List<UserRepresentation> users = this.keycloak.realm(REALM_NAME).users().searchByEmail(newAccount.getEmail(), true);
        log.info("Users found by email {}", users.stream()
                .map(UserRepresentation::getEmail)
                .collect(Collectors.toList()));

        if(!users.isEmpty()){
            throw new AccountAlreadyCreatedException(newAccount.getEmail());
        }

        UserResource userResource = createUserResource(newAccount);

        CredentialRepresentation credential = getCredentialRepresentation(newAccount);
        userResource.resetPassword(credential);

        setUserRoles(userResource);

        log.info(userResource.toString());
       return null;
    }

    private void setUserRoles(UserResource userResource) {
        RoleRepresentation userRealmRole = this.keycloak.realm(REALM_NAME).roles()
                .get("user").toRepresentation();
        userResource.roles().realmLevel().add(Collections.singletonList(userRealmRole));

        ClientRepresentation accountsMicroClient = this.keycloak.realm(REALM_NAME).clients()
                .findByClientId("accounts-micro").get(0);

        RoleRepresentation userClientRole = this.keycloak.realm(REALM_NAME).clients().get(accountsMicroClient.getId())
                .roles().get("client_user").toRepresentation();
        userResource.roles().clientLevel(accountsMicroClient.getId()).add(Collections.singletonList(userClientRole));
    }

    private UserResource createUserResource(Account newAccount) {
        UserRepresentation user = getUserRepresentation(newAccount);

        Response response = this.keycloak.realm(REALM_NAME).users().create(user);
        log.info("Response: "+response.getStatus()+" "+response.getStatusInfo()+" "+response.getLocation());

        String userId = CreatedResponseUtil.getCreatedId(response);
        log.info("User created with id "+userId);
        return this.keycloak.realm(REALM_NAME).users().get(userId);
    }

    private static CredentialRepresentation getCredentialRepresentation(Account newAccount) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(newAccount.getPassword());
        return credential;
    }

    private static UserRepresentation getUserRepresentation(Account newAccount) {
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setEmail(newAccount.getEmail());
        user.setFirstName(newAccount.getName());
        user.setLastName(newAccount.getSurname());
        user.setAttributes(Collections.singletonMap("birthdate", List.of(newAccount.getBirthdate().toString())));
        user.setAttributes(Collections.singletonMap("gender", List.of(newAccount.getGender().toString())));
        return user;
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

}
