package com.sergiostefanizzi.accountmicroservice.service;

import com.sergiostefanizzi.accountmicroservice.controller.converter.UserRepresentationToAccountConverter;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AccountNotFoundException;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AdminAlreadyCreatedException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@Slf4j
class AdminsServiceTest {
    @Mock
    private KeycloakService keycloakService;
    @Mock
    private UserRepresentationToAccountConverter userRepresentationToAccountConverter;
    @InjectMocks
    private AdminsService adminsService;

    private UserRepresentation userRepresentation;
    private UserRepresentation userRepresentation1;
    private UserRepresentation userRepresentation2;

    private Account convertedAccount;
    private Account convertedAccount1;
    private Account convertedAccountDisabled;

    String accountId = UUID.randomUUID().toString();
    String accountId1 = UUID.randomUUID().toString();
    String accountId2 = UUID.randomUUID().toString();
    @BeforeEach
    void setUp() {
        Account account = new Account("pinco.pallino@prova.com",
                LocalDate.of(1990, 4, 4),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        account.setName("Pinco");
        account.setSurname("Pallino");
        account.setId(this.accountId);

        this.userRepresentation = new UserRepresentation();
        this.userRepresentation.setId(account.getId());
        this.userRepresentation.setEnabled(true);
        this.userRepresentation.setEmail(account.getEmail());
        this.userRepresentation.setFirstName(account.getName());
        this.userRepresentation.setLastName(account.getSurname());
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("birthdate", List.of(account.getBirthdate().toString()));
        attributes.put("gender", List.of(account.getGender().toString()));
        this.userRepresentation.setAttributes(attributes);
        this.userRepresentation.setEmailVerified(true);
        this.userRepresentation.setId(account.getId());

        this.convertedAccount = new Account(
                account.getEmail(),
                account.getBirthdate(),
                account.getGender(),
                null
        );
        this.convertedAccount.setId(account.getId());
        this.convertedAccount.setName(account.getName());
        this.convertedAccount.setSurname(account.getSurname());

        account.setEmail("pinco.pallino2@prova.com");
        account.setId(this.accountId1);

        this.userRepresentation1 = new UserRepresentation();
        this.userRepresentation1.setId(account.getId());
        this.userRepresentation1.setEnabled(true);
        this.userRepresentation1.setEmail(account.getEmail());
        this.userRepresentation1.setFirstName(account.getName());
        this.userRepresentation1.setLastName(account.getSurname());
        this.userRepresentation1.setAttributes(attributes);
        this.userRepresentation1.setEmailVerified(true);
        this.userRepresentation1.setId(account.getId());

        this.convertedAccount1 = new Account(
                account.getEmail(),
                account.getBirthdate(),
                account.getGender(),
                null
        );
        this.convertedAccount1.setId(account.getId());
        this.convertedAccount1.setName(account.getName());
        this.convertedAccount1.setSurname(account.getSurname());

        account.setEmail("pinco.pallino3@prova.com");
        account.setId(this.accountId2);

        this.userRepresentation2 = new UserRepresentation();
        this.userRepresentation2.setId(account.getId());
        this.userRepresentation2.setEnabled(false);
        this.userRepresentation2.setEmail(account.getEmail());
        this.userRepresentation2.setFirstName(account.getName());
        this.userRepresentation2.setLastName(account.getSurname());
        this.userRepresentation2.setAttributes(attributes);
        this.userRepresentation2.setEmailVerified(true);
        this.userRepresentation2.setId(account.getId());

        this.convertedAccountDisabled = new Account(
                account.getEmail(),
                account.getBirthdate(),
                account.getGender(),
                null
        );
        this.convertedAccountDisabled.setId(account.getId());
        this.convertedAccountDisabled.setName(account.getName());
        this.convertedAccountDisabled.setSurname(account.getSurname());
    }

    @AfterEach
    void tearDown() {
    }

    //FIND ALL
    @Test
    void testFindAllActive_Success() {
        List<UserRepresentation> userList = List.of(this.userRepresentation, this.userRepresentation1);
        when(this.keycloakService.findAllActive(false)).thenReturn(userList);
        when(this.userRepresentationToAccountConverter.convert(userList.get(0))).thenReturn(this.convertedAccount);
        when(this.userRepresentationToAccountConverter.convert(userList.get(1))).thenReturn(this.convertedAccount1);

        List<Account> accountList = this.adminsService.findAll(false);

        log.info(accountList.toString());
        assertEquals(List.of(this.convertedAccount, this.convertedAccount1), accountList);
        verify(this.keycloakService, times(1)).findAllActive(anyBoolean());
        verify(this.userRepresentationToAccountConverter, times(2)).convert(any(UserRepresentation.class));

    }


    @Test
    void testFindAllRemoved_Success() {
        List<UserRepresentation> userList = List.of(this.userRepresentation, this.userRepresentation1, this.userRepresentation2);
        when(this.keycloakService.findAllActive(false)).thenReturn(userList);
        when(this.userRepresentationToAccountConverter.convert(userList.get(0))).thenReturn(this.convertedAccount);
        when(this.userRepresentationToAccountConverter.convert(userList.get(1))).thenReturn(this.convertedAccount1);
        when(this.userRepresentationToAccountConverter.convert(userList.get(2))).thenReturn(this.convertedAccountDisabled);

        List<Account> accountList = this.adminsService.findAll(false);


        log.info(accountList.toString());
        assertEquals(List.of(this.convertedAccount, this.convertedAccount1, this.convertedAccountDisabled), accountList);
        verify(this.keycloakService, times(1)).findAllActive(anyBoolean());
        verify(this.userRepresentationToAccountConverter, times(3)).convert(any(UserRepresentation.class));
    }

    //SAVE
    @Test
    void testSave_Success(){
        when(this.keycloakService.createAdmin(anyString())).thenReturn(Optional.of(this.accountId));

        String adminId = this.adminsService.save(this.accountId);

        assertEquals(this.accountId, adminId);
        log.info("New admin created "+adminId);
        verify(this.keycloakService, times(1)).createAdmin(anyString());
    }


    @Test
    void testSave_AlreadyCreated_Failed(){
        when(this.keycloakService.createAdmin(anyString())).thenReturn(Optional.empty());

        assertThrows(AdminAlreadyCreatedException.class, () ->
                this.adminsService.save(this.accountId)
        );

        verify(this.keycloakService, times(1)).createAdmin(anyString());

    }

    //REMOVE
    @Test
    void testRemove_ByAdmin_Success(){
        when(this.keycloakService.blockUser(anyString())).thenReturn(Optional.of(this.accountId));

        this.adminsService.remove(this.accountId);

        verify(this.keycloakService, times(1)).blockUser(anyString());
    }


    @Test
    void testRemove_ByAdmin_Failed(){
        when(this.keycloakService.blockUser(anyString())).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () ->
                this.adminsService.remove(this.accountId)
        );

        verify(this.keycloakService, times(1)).blockUser(anyString());
    }



}