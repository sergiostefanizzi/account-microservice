package com.sergiostefanizzi.accountmicroservice.service;

import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.AccountPatch;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakService {
    @Autowired
    private Keycloak keycloak;
    private final String REALM_NAME = "social-accounts";

    public Boolean checksEmailValidated(String accountId) {
        try{
            UserRepresentation user = this.keycloak.realm(REALM_NAME)
                    .users()
                    .get(accountId)
                    .toRepresentation();
            return user.isEmailVerified();
        }catch (NotFoundException ex){
            log.info(ex.getMessage());
            return false;
        }
    }

    public Boolean checkActiveById(String accountId) {
        try{
            UserRepresentation user = this.keycloak.realm(REALM_NAME)
                    .users()
                    .get(accountId)
                    .toRepresentation();
            return user.isEnabled();
        }catch (NotFoundException ex){
            log.info(ex.getMessage());
            return false;
        }
    }

    public UserRepresentation updateUser(String accountId, AccountPatch accountToUpdate) {
        UserResource userResource = this.keycloak.realm(REALM_NAME)
                .users()
                .get(accountId);
        UserRepresentation user = userResource.toRepresentation();

        Map<String, List<String>> attributes = new HashMap<>();
        attributes = user.getAttributes();

        // check perche' modifico solo i campi passati dalla patch
        if (StringUtils.hasText(accountToUpdate.getName()))  user.setFirstName(accountToUpdate.getName());
        if (StringUtils.hasText(accountToUpdate.getSurname()))  user.setLastName((accountToUpdate.getSurname()));
        if (StringUtils.hasText(accountToUpdate.getGender().toString())){
            attributes.put("gender", List.of(Account.GenderEnum.valueOf(accountToUpdate.getGender().toString()).toString()));
            user.setAttributes(attributes);
        }

        if (StringUtils.hasText(accountToUpdate.getPassword())){
            user.setCredentials(
                    Collections.singletonList(getCredentialRepresentation(accountToUpdate.getPassword()))
            );
        };

        userResource.update(user);
        return user;
    }

    public boolean validateEmail(String accountId, String validationCode) {
        return true;
    }

    public UserRepresentation createUser(Account newAccount){
        UserResource userResource = createUserResource(newAccount);

        CredentialRepresentation credential = getCredentialRepresentation(newAccount.getPassword());

        userResource.resetPassword(credential);

        setUserRoles(userResource);

        log.info(userResource.toRepresentation().toString());
        return userResource.toRepresentation();
    }

    public void removeUser(String accountId) {
        UserRepresentation user = this.keycloak.realm(REALM_NAME)
                .users()
                .get(accountId)
                .toRepresentation();

        user.setEnabled(false);

        this.keycloak.realm(REALM_NAME)
                .users()
                .get(accountId)
                .update(user);

    }

    public List<UserRepresentation> getUsersByEmail(String email){
        List<UserRepresentation> usersList = this.keycloak.realm(REALM_NAME)
                .users()
                .searchByEmail(email, true);


        log.info("Users found by email {}", usersList
                .stream()
                .map(UserRepresentation::getEmail)
                .collect(Collectors.toList()));

        return usersList;
    }

    private UserResource createUserResource(Account newAccount) {
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setEmail(newAccount.getEmail());
        user.setFirstName(newAccount.getName());
        user.setLastName(newAccount.getSurname());
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("birthdate", List.of(newAccount.getBirthdate().toString()));
        attributes.put("gender", List.of(newAccount.getGender().toString()));
        user.setAttributes(attributes);
        //TODO togliere questo e fare la verifica via email
        user.setEmailVerified(true);

        Response response = this.keycloak.realm(REALM_NAME).users().create(user);
        log.info("Response: "+response.getStatus()+" "+response.getStatusInfo()+" "+response.getLocation());

        String userId = CreatedResponseUtil.getCreatedId(response);
        log.info("User created with id "+userId);
        return this.keycloak.realm(REALM_NAME).users().get(userId);
    }


    private void setUserRoles(UserResource userResource) {
        RoleRepresentation userRealmRole = this.keycloak.realm(REALM_NAME)
                .roles()
                .get("user")
                .toRepresentation();
        userResource
                .roles()
                .realmLevel()
                .add(Collections.singletonList(userRealmRole));

        ClientRepresentation accountsMicroClient = this.keycloak.realm(REALM_NAME)
                .clients()
                .findByClientId("accounts-micro")
                .get(0);

        RoleRepresentation userClientRole = this.keycloak.realm(REALM_NAME)
                .clients()
                .get(accountsMicroClient.getId())
                .roles()
                .get("client_user")
                .toRepresentation();
        userResource
                .roles()
                .clientLevel(accountsMicroClient.getId())
                .add(Collections.singletonList(userClientRole));
    }



    private static CredentialRepresentation getCredentialRepresentation(String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        return credential;
    }



}
