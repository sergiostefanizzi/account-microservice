package com.sergiostefanizzi.accountmicroservice.service;

import com.sergiostefanizzi.accountmicroservice.model.Account;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakService {
    @Autowired
    private Keycloak keycloak;
    private final String REALM_NAME = "social-accounts";


    public UserRepresentation createUser(Account newAccount){
        UserResource userResource = createUserResource(newAccount);

        CredentialRepresentation credential = getCredentialRepresentation(newAccount);

        userResource.resetPassword(credential);

        setUserRoles(userResource);

        log.info(userResource.toRepresentation().toString());
        return userResource.toRepresentation();
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



    private static CredentialRepresentation getCredentialRepresentation(Account newAccount) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(newAccount.getPassword());
        return credential;
    }




}
