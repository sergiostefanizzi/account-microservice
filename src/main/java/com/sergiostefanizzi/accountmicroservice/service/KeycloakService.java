package com.sergiostefanizzi.accountmicroservice.service;

import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.AccountPatch;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AccountAlreadyCreatedException;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AccountNotFoundException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
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

    public UserRepresentation createUser(Account newAccount, String validationCode){
        RealmResource realmResource = this.keycloak.realm(REALM_NAME);

        UserResource userResource = createUserResource(realmResource, newAccount, validationCode);

        CredentialRepresentation credential = getCredentialRepresentation(newAccount.getPassword());

        userResource.resetPassword(credential);

        setRoles(userResource, realmResource, "user",false);

        return userResource.toRepresentation();
    }

    public void removeUser(String accountId) {
        RealmResource realmResource = this.keycloak
                .realm(REALM_NAME);

        UserResource userResource = realmResource
                .users()
                .get(accountId);

        UserRepresentation user = userResource.toRepresentation();

        setRoles(userResource, realmResource, "admin",true);

        user.setEnabled(false);
        user.setEmailVerified(false);

        userResource.update(user);
    }

    public Boolean checkUsersByEmail(String email){
        Optional<UserRepresentation> userOptional = this.keycloak
                .realm(REALM_NAME)
                .users()
                .searchByEmail(email, true)
                .stream()
                .findFirst();

        if (userOptional.isEmpty()){
            return false;
        }
        log.info("User found by email {}", userOptional.get().getEmail());
        return true;
    }

    public UserRepresentation updateUser(String accountId, AccountPatch accountToUpdate) {
        UserResource userResource = this.keycloak
                .realm(REALM_NAME)
                .users()
                .get(accountId);
        UserRepresentation user = userResource.toRepresentation();

        Map<String, List<String>> attributes = user.getAttributes();

        // check perche' modifico solo i campi passati dalla patch
        if (StringUtils.hasText(accountToUpdate.getName()))  user.setFirstName(accountToUpdate.getName());
        if (StringUtils.hasText(accountToUpdate.getSurname()))  user.setLastName((accountToUpdate.getSurname()));
        if (accountToUpdate.getGender() != null){
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

        UserResource userResource = this.keycloak
                .realm(REALM_NAME)
                .users()
                .get(accountId);
        UserRepresentation user = userResource.toRepresentation();

        Map<String, List<String>> attributes = user.getAttributes();
        String savedValidationCode = attributes.get("validationCode").get(0);
        if (savedValidationCode.equals(validationCode)){
            user.setEmailVerified(true);
            userResource.update(user);
            return true;
        }
        return false;
    }

    public Optional<String> createAdmin(String accountId) {
        RealmResource realmResource = this.keycloak.realm(REALM_NAME);

        UserResource userResource = realmResource
                .users()
                .get(accountId);

        UserRepresentation user = userResource.toRepresentation();


        if(!setRoles(userResource, realmResource, "admin",false)){
            return Optional.empty();
        }

        return Optional.of(user.getId());
    }

    public Optional<String> blockUser(String accountId) {
        RealmResource realmResource = this.keycloak.realm(REALM_NAME);

        UserResource userResource = realmResource
                .users()
                .get(accountId);

        UserRepresentation user = userResource.toRepresentation();

        setRoles(userResource, realmResource, "admin",true);

        if(!user.isEnabled()){
            return Optional.empty();
        }
        user.setEnabled(false);
        user.setEmailVerified(false);

        userResource
                .update(user);
        return Optional.of(user.getId());
    }


    public List<UserRepresentation> findAllActive(Boolean disabled){
        List<UserRepresentation> usersList = this.keycloak.realm(REALM_NAME)
                .users()
                .list();
        if (!disabled){
            return usersList.stream().filter(UserRepresentation::isEnabled).toList();
        }
        return usersList;
    }


    private UserResource createUserResource(RealmResource realmResource, Account newAccount, String validationCode) {
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setEmail(newAccount.getEmail());
        user.setFirstName(newAccount.getName());
        user.setLastName(newAccount.getSurname());
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("birthdate", List.of(newAccount.getBirthdate().toString()));
        attributes.put("gender", List.of(newAccount.getGender().toString()));
        attributes.put("validationCode", List.of(validationCode));
        user.setAttributes(attributes);
        user.setEmailVerified(false);

        UsersResource userResource = realmResource.users();

        Response response = userResource.create(user);


        //String userId = CreatedResponseUtil.getCreatedId(response);
        if(response.getStatus() != 201){
            throw new AccountAlreadyCreatedException(newAccount.getEmail());
        }
        String userId = getCreatedId(response);
        log.info("User created with id "+userId+" validation code --> "+validationCode);
        return userResource.get(userId);
    }

    private static String getCreatedId(Response response) {
        URI location = response.getLocation();
        String path = location.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }


    private Boolean setRoles(UserResource userResource, RealmResource realmResource, String role, Boolean isUnset) {
        RoleRepresentation userRealmRole = realmResource
                .roles()
                .get(role)
                .toRepresentation();

        RoleMappingResource roles = userResource
                .roles();

        RoleScopeResource roleScopeResource = roles
                .realmLevel();

        if(isUnset){
            if(roleScopeResource.listEffective().contains(userRealmRole)){
                roleScopeResource
                        .remove(Collections.singletonList(userRealmRole));
            }else{
                return false;
            }
        }else{
            if(!roleScopeResource.listEffective().contains(userRealmRole)){
                roleScopeResource.add(Collections.singletonList(userRealmRole));
            }else{
                return false;
            }

        }

        /*
        if(roleScopeResource
                .listEffective()
                .contains(userRealmRole)){
            if(isUnset){
                roleScopeResource
                        .remove(Collections.singletonList(userRealmRole));
            }else{
                return false;
            }
        }else{
            roleScopeResource
                    .add(Collections.singletonList(userRealmRole));
        }

         */
        return true;
    }





    private static CredentialRepresentation getCredentialRepresentation(String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        return credential;
    }



}
