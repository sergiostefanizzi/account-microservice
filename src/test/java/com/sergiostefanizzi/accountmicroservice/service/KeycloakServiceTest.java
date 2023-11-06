package com.sergiostefanizzi.accountmicroservice.service;

import com.sergiostefanizzi.accountmicroservice.controller.converter.UserRepresentationToAccountConverter;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.AccountPatch;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AccountAlreadyCreatedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@Slf4j
class KeycloakServiceTest {
    @Mock
    private Keycloak keycloak;
    @Mock
    private RealmResource realmResource;
    @Mock
    private UserResource userResource;
    @Mock
    private UsersResource usersResource;
    @Mock
    private RolesResource rolesResource;
    @Mock
    private RoleResource roleResource;
    @Mock
    private RoleRepresentation roleRepresentation;
    @Mock
    private RoleMappingResource roleMappingResource;
    @Mock
    private RoleScopeResource roleScopeResource;
    @Mock
    private ClientsResource clientsResource;
    @Mock
    private ClientResource clientResource;
    @Mock
    private ClientRepresentation clientRepresentation;
    @Mock
    private Response response;
    @Mock
    private Response.StatusType statusType;
    @InjectMocks
    private KeycloakService keycloakService;
    private Account account;
    private UserRepresentation userRepresentation;
    private final String REALM_NAME = "social-accounts";
    String accountId = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        this.account = new Account("pinco.pallino@prova.com",
                LocalDate.of(1990,4,4),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        this.account.setName("Pinco");
        this.account.setSurname("Pallino");
        this.account.setId(this.accountId);

        this.userRepresentation = new UserRepresentation();
        this.userRepresentation.setId(this.account.getId());
        this.userRepresentation.setEnabled(true);
        this.userRepresentation.setEmail(this.account.getEmail());
        this.userRepresentation.setFirstName(this.account.getName());
        this.userRepresentation.setLastName(this.account.getSurname());
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("birthdate", List.of(this.account.getBirthdate().toString()));
        attributes.put("gender", List.of(this.account.getGender().toString()));
        this.userRepresentation.setAttributes(attributes);
        this.userRepresentation.setEmailVerified(true);
    }

    @Test
    void testChecksEmailValidated_Success(){
        when(this.keycloak.realm(anyString())).thenReturn(this.realmResource);
        when(this.realmResource.users()).thenReturn(this.usersResource);
        when(this.usersResource.get(anyString())).thenReturn(this.userResource);
        when(this.userResource.toRepresentation()).thenReturn(this.userRepresentation);

        Boolean isEmailValidated = this.keycloakService.checksEmailValidated(this.account.getEmail());

        assertTrue(isEmailValidated);
        log.info("Email is verified: "+isEmailValidated);
        verify(this.keycloak, times(1)).realm(anyString());
        verify(this.realmResource, times(1)).users();
        verify(this.usersResource, times(1)).get(anyString());
        verify(this.userResource, times(1)).toRepresentation();
    }

    @Test
    void testChecksEmailValidated_Failed(){
        this.userRepresentation.setEmailVerified(false);

        when(this.keycloak.realm(anyString())).thenReturn(this.realmResource);
        when(this.realmResource.users()).thenReturn(this.usersResource);
        when(this.usersResource.get(anyString())).thenReturn(this.userResource);
        when(this.userResource.toRepresentation()).thenReturn(this.userRepresentation);

        Boolean isEmailValidated = this.keycloakService.checksEmailValidated(this.account.getEmail());

        assertFalse(isEmailValidated);
        log.info("Email is verified: "+isEmailValidated);
        verify(this.keycloak, times(1)).realm(anyString());
        verify(this.realmResource, times(1)).users();
        verify(this.usersResource, times(1)).get(anyString());
        verify(this.userResource, times(1)).toRepresentation();
    }

    @Test
    void testChecksEmailValidated_NotFoundException_Failed(){
        this.userRepresentation.setEmailVerified(false);

        when(this.keycloak.realm(anyString())).thenReturn(this.realmResource);
        when(this.realmResource.users()).thenReturn(this.usersResource);
        when(this.usersResource.get(anyString())).thenThrow(NotFoundException.class);

        Boolean isEmailValidated = this.keycloakService.checksEmailValidated(this.account.getEmail());

        assertFalse(isEmailValidated);
        log.info("Email is verified: "+isEmailValidated);
        verify(this.keycloak, times(1)).realm(anyString());
        verify(this.realmResource, times(1)).users();
        verify(this.usersResource, times(1)).get(anyString());
        verify(this.userResource, times(0)).toRepresentation();
    }

    @Test
    void testCheckActiveById_Success(){
        when(this.keycloak.realm(anyString())).thenReturn(this.realmResource);
        when(this.realmResource.users()).thenReturn(this.usersResource);
        when(this.usersResource.get(anyString())).thenReturn(this.userResource);
        when(this.userResource.toRepresentation()).thenReturn(this.userRepresentation);

        Boolean isActive = this.keycloakService.checkActiveById(this.account.getId());

        assertTrue(isActive);
        log.info("Account is active: "+ isActive);
        verify(this.keycloak, times(1)).realm(anyString());
        verify(this.realmResource, times(1)).users();
        verify(this.usersResource, times(1)).get(anyString());
        verify(this.userResource, times(1)).toRepresentation();
    }

    @Test
    void testCheckActiveById_Failed(){
        this.userRepresentation.setEnabled(false);

        when(this.keycloak.realm(anyString())).thenReturn(this.realmResource);
        when(this.realmResource.users()).thenReturn(this.usersResource);
        when(this.usersResource.get(anyString())).thenReturn(this.userResource);
        when(this.userResource.toRepresentation()).thenReturn(this.userRepresentation);

        Boolean isActive = this.keycloakService.checkActiveById(this.account.getId());

        assertFalse(isActive);
        log.info("Account is active: "+ isActive);
        verify(this.keycloak, times(1)).realm(anyString());
        verify(this.realmResource, times(1)).users();
        verify(this.usersResource, times(1)).get(anyString());
        verify(this.userResource, times(1)).toRepresentation();
    }

    @Test
    void testCheckActiveById_NotFoundException_Failed(){
        this.userRepresentation.setEnabled(false);

        when(this.keycloak.realm(anyString())).thenReturn(this.realmResource);
        when(this.realmResource.users()).thenReturn(this.usersResource);
        when(this.usersResource.get(anyString())).thenThrow(NotFoundException.class);

        Boolean isActive = this.keycloakService.checkActiveById(this.account.getId());

        assertFalse(isActive);
        log.info("Account is active: "+ isActive);
        verify(this.keycloak, times(1)).realm(anyString());
        verify(this.realmResource, times(1)).users();
        verify(this.usersResource, times(1)).get(anyString());
        verify(this.userResource, times(0)).toRepresentation();
    }


    @Test
    void testCreateUser_Success() {
        this.account.setId(null);
        RoleRepresentation newRole = new RoleRepresentation("user","User role",false);
        RoleRepresentation newClientRole = new RoleRepresentation("client_user","Client User role",false);
        ClientRepresentation newClient = new ClientRepresentation();
        newClient.setName("accounts-micro");

        when(this.keycloak.realm(anyString())).thenReturn(this.realmResource);
        when(this.realmResource.users()).thenReturn(this.usersResource);
        when(this.usersResource.create(any(UserRepresentation.class))).thenReturn(this.response);
        when(this.response.getStatus()).thenReturn(201);
        when(this.response.getLocation()).thenReturn(URI.create("http://localhost:8082/admin/realms/social-accounts/users/90d2018d-8ae7-41b8-a266-5f992d0e1b20"));
        when(this.usersResource.get(anyString())).thenReturn(this.userResource);

        doNothing().when(this.userResource).resetPassword(any(CredentialRepresentation.class));

        when(this.realmResource.roles()).thenReturn(this.rolesResource);
        when(this.rolesResource.get(anyString())).thenReturn(this.roleResource);
        when(this.roleResource.toRepresentation()).thenReturn(newRole);

        when(this.userResource.roles()).thenReturn(this.roleMappingResource);
        when(this.roleMappingResource.realmLevel()).thenReturn(this.roleScopeResource);

        when(this.realmResource.clients()).thenReturn(this.clientsResource);
        when(this.clientsResource.findByClientId("accounts-micro")).thenReturn(Collections.singletonList(newClient));

        when(this.clientsResource.get(newClient.getId())).thenReturn(this.clientResource);
        when(this.clientResource.roles()).thenReturn(this.rolesResource);

        when(this.roleResource.toRepresentation()).thenReturn(newClientRole);

        when(this.roleMappingResource.clientLevel(newClient.getId())).thenReturn(this.roleScopeResource);

        when(this.userResource.toRepresentation()).thenReturn(this.userRepresentation);

        UserRepresentation returnedUser = this.keycloakService.createUser(this.account);

        assertEquals(returnedUser, this.userRepresentation);
        log.info(returnedUser.getEmail());
        verify(this.keycloak, times(3)).realm(anyString());
        verify(this.realmResource, times(2)).users();
        verify(this.usersResource, times(1)).create(any(UserRepresentation.class));
        verify(this.response, times(1)).getStatus();
        verify(this.response, times(1)).getLocation();
        verify(this.usersResource, times(1)).get(anyString());
        verify(this.userResource, times(1)).resetPassword(any(CredentialRepresentation.class));
        verify(this.realmResource, times(1)).roles();
        verify(this.rolesResource, times(2)).get(anyString());
        verify(this.roleResource, times(2)).toRepresentation();
        verify(this.userResource, times(2)).roles();
        verify(this.roleMappingResource, times(1)).realmLevel();
        verify(this.realmResource, times(2)).clients();
        verify(this.clientsResource, times(1)).findByClientId(anyString());
        verify(this.clientsResource, times(1)).get(newClient.getId());
        verify(this.clientResource, times(1)).roles();
        verify(this.roleMappingResource, times(1)).clientLevel(newClient.getId());
        verify(this.userResource, times(2)).toRepresentation();
    }

    @Test
    void testCreateUser_Failed() {
        this.account.setId(null);
        ClientRepresentation newClient = new ClientRepresentation();
        newClient.setName("accounts-micro");

        when(this.keycloak.realm(anyString())).thenReturn(this.realmResource);
        when(this.realmResource.users()).thenReturn(this.usersResource);
        when(this.usersResource.create(any(UserRepresentation.class))).thenReturn(this.response);
        when(this.response.getStatus()).thenReturn(409);

        assertThrows(AccountAlreadyCreatedException.class,
                () -> this.keycloakService.createUser(this.account));

        verify(this.keycloak, times(1)).realm(anyString());
        verify(this.realmResource, times(1)).users();
        verify(this.usersResource, times(1)).create(any(UserRepresentation.class));
        verify(this.response, times(1)).getStatus();
        verify(this.response, times(0)).getLocation();
        verify(this.usersResource, times(0)).get(anyString());
        verify(this.userResource, times(0)).resetPassword(any(CredentialRepresentation.class));
        verify(this.realmResource, times(0)).roles();
        verify(this.rolesResource, times(0)).get(anyString());
        verify(this.roleResource, times(0)).toRepresentation();
        verify(this.userResource, times(0)).roles();
        verify(this.roleMappingResource, times(0)).realmLevel();
        verify(this.realmResource, times(0)).clients();
        verify(this.clientsResource, times(0)).findByClientId(anyString());
        verify(this.clientsResource, times(0)).get(newClient.getId());
        verify(this.clientResource, times(0)).roles();
        verify(this.roleMappingResource, times(0)).clientLevel(newClient.getId());
        verify(this.userResource, times(0)).toRepresentation();
    }

    @Test
    void testRemoveUser_Success(){
        when(this.keycloak.realm(anyString())).thenReturn(this.realmResource);
        when(this.realmResource.users()).thenReturn(this.usersResource);
        when(this.usersResource.get(anyString())).thenReturn(this.userResource);
        when(this.userResource.toRepresentation()).thenReturn(this.userRepresentation);

        this.keycloakService.removeUser(this.account.getId());

        verify(this.keycloak, times(1)).realm(anyString());
        verify(this.realmResource, times(1)).users();
        verify(this.usersResource, times(1)).get(anyString());
        verify(this.userResource, times(1)).toRepresentation();
    }

    @Test
    void testRemoveUser_NotFound_Failed(){
        when(this.keycloak.realm(anyString())).thenReturn(this.realmResource);
        when(this.realmResource.users()).thenReturn(this.usersResource);
        when(this.usersResource.get(anyString())).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class,
                () -> this.keycloakService.removeUser(this.account.getId()));

        verify(this.keycloak, times(1)).realm(anyString());
        verify(this.realmResource, times(1)).users();
        verify(this.usersResource, times(1)).get(anyString());
        verify(this.userResource, times(0)).toRepresentation();
    }

    @Test
    void testCheckUsersByEmail_Success(){
        when(this.keycloak.realm(anyString())).thenReturn(this.realmResource);
        when(this.realmResource.users()).thenReturn(this.usersResource);
        when(this.usersResource.searchByEmail(anyString(), anyBoolean())).thenReturn(List.of(this.userRepresentation));

        Boolean check = this.keycloakService.checkUsersByEmail(this.account.getEmail());

        assertTrue(check);
        verify(this.keycloak, times(1)).realm(anyString());
        verify(this.realmResource, times(1)).users();
        verify(this.usersResource, times(1)).searchByEmail(anyString(), anyBoolean());
    }

    @Test
    void testCheckUsersByEmail_EmailNotFound_Failed(){
        when(this.keycloak.realm(anyString())).thenReturn(this.realmResource);
        when(this.realmResource.users()).thenReturn(this.usersResource);
        when(this.usersResource.searchByEmail(anyString(), anyBoolean())).thenReturn(List.of());

        Boolean check = this.keycloakService.checkUsersByEmail(this.account.getEmail());

        assertFalse(check);
        verify(this.keycloak, times(1)).realm(anyString());
        verify(this.realmResource, times(1)).users();
        verify(this.usersResource, times(1)).searchByEmail(anyString(), anyBoolean());
    }

    @Test
    void testUpdateUser_Success(){
        AccountPatch accountToUpdate = new AccountPatch();
        accountToUpdate.setName("Alessia");
        accountToUpdate.setSurname("Verdi");
        accountToUpdate.setGender(AccountPatch.GenderEnum.FEMALE);
        accountToUpdate.setPassword("43hg434j5g4!");

        when(this.keycloak.realm(anyString())).thenReturn(this.realmResource);
        when(this.realmResource.users()).thenReturn(this.usersResource);
        when(this.usersResource.get(anyString())).thenReturn(this.userResource);
        when(this.userResource.toRepresentation()).thenReturn(this.userRepresentation);

        UserRepresentation userUpdated = this.keycloakService.updateUser(this.account.getId(), accountToUpdate);

        assertEquals(userUpdated.getId(), this.account.getId());
        assertEquals(userUpdated.getFirstName(), accountToUpdate.getName());
        assertEquals(userUpdated.getLastName(), accountToUpdate.getSurname());
        assertEquals(userUpdated.getAttributes().get("gender").get(0), accountToUpdate.getGender().toString());
        assertEquals(userUpdated.getCredentials().get(0).getValue(), accountToUpdate.getPassword());
        log.info("New Password --> {}",userUpdated.getCredentials().get(0).getValue());
        verify(this.keycloak, times(1)).realm(anyString());
        verify(this.realmResource, times(1)).users();
        verify(this.usersResource, times(1)).get(anyString());
        verify(this.userResource, times(1)).toRepresentation();
    }

    @Test
    void testUpdateUser_NotFound_Failed(){
        AccountPatch accountToUpdate = new AccountPatch();
        accountToUpdate.setName("Alessia");
        accountToUpdate.setSurname("Verdi");
        accountToUpdate.setGender(AccountPatch.GenderEnum.FEMALE);
        accountToUpdate.setPassword("43hg434j5g4!");

        when(this.keycloak.realm(anyString())).thenReturn(this.realmResource);
        when(this.realmResource.users()).thenReturn(this.usersResource);
        when(this.usersResource.get(anyString())).thenThrow(NotFoundException.class);


        assertThrows(NotFoundException.class,
                () -> this.keycloakService.updateUser(this.account.getId(), accountToUpdate));


        verify(this.keycloak, times(1)).realm(anyString());
        verify(this.realmResource, times(1)).users();
        verify(this.usersResource, times(1)).get(anyString());
        verify(this.userResource, times(0)).toRepresentation();
    }
}