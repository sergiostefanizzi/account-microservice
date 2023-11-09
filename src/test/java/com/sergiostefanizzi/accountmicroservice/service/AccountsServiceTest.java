package com.sergiostefanizzi.accountmicroservice.service;

import com.sergiostefanizzi.accountmicroservice.controller.converter.AccountToJpaConverter;
import com.sergiostefanizzi.accountmicroservice.controller.converter.UserRepresentationToAccountConverter;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AccountAlreadyCreatedException;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AccountNotActivedException;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.AccountPatch;
import com.sergiostefanizzi.accountmicroservice.repository.AccountsRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@Slf4j
class AccountsServiceTest {
    @Mock
    private KeycloakService keycloakService;
    @Mock
    private UserRepresentationToAccountConverter userRepresentationToAccountConverter;
    @InjectMocks
    private AccountsService accountsService;

    private Account account;
    private UserRepresentation userRepresentation;

    private Account convertedAccount;

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
        this.userRepresentation.setId(this.account.getId());

        this.convertedAccount = new Account(
                this.account.getEmail(),
                this.account.getBirthdate(),
                this.account.getGender(),
                null
        );
        this.convertedAccount.setId(this.account.getId());
        this.convertedAccount.setName(this.account.getName());
        this.convertedAccount.setSurname(this.account.getSurname());
    }

    @AfterEach
    void tearDown() {
    }


    // SAVE ACCOUNT
    @Test
    void testSave_Success() {
        this.account.setId(null);
        when(this.keycloakService.checkUsersByEmail(anyString())).thenReturn(false);
        when(this.keycloakService.createUser(any(Account.class))).thenReturn(this.userRepresentation);
        when(this.userRepresentationToAccountConverter.convert(any(UserRepresentation.class))).thenReturn(this.convertedAccount);

        Account savedAccount = this.accountsService.save(this.account);

        assertEquals(this.convertedAccount, savedAccount);
        log.info(savedAccount.toString());
        verify(this.keycloakService, times(1)).checkUsersByEmail(anyString());
        verify(this.keycloakService, times(1)).createUser(any(Account.class));
        verify(this.userRepresentationToAccountConverter, times(1)).convert(any(UserRepresentation.class));
    }

    @Test
    void testSaveAccount_Failed(){
        this.account.setId(null);
        when(this.keycloakService.checkUsersByEmail(anyString())).thenReturn(true);

        assertThrows(AccountAlreadyCreatedException.class, () ->
                this.accountsService.save(this.account)
        );

        verify(this.keycloakService, times(1)).checkUsersByEmail(anyString());
        verify(this.keycloakService, times(0)).createUser(any(Account.class));
        verify(this.userRepresentationToAccountConverter, times(0)).convert(any(UserRepresentation.class));

    }

    // DELETE ACCOUNT
    @Test
    void testRemove_Success(){
        this.accountsService.remove(this.account.getId());

        verify(this.keycloakService, times(1)).removeUser(anyString());
    }



    // UPDATE ACCOUNT

    @Test
    void testUpdate_Success(){
        AccountPatch accountToUpdate = new AccountPatch();
        accountToUpdate.setName("Alessia");
        accountToUpdate.setSurname("Verdi");
        accountToUpdate.setGender(AccountPatch.GenderEnum.FEMALE);
        accountToUpdate.setPassword("43hg434j5g4!");

        this.userRepresentation.setFirstName(accountToUpdate.getName());
        this.userRepresentation.setLastName(accountToUpdate.getSurname());
        Map<String, List<String>> attributes = this.userRepresentation.getAttributes();
        attributes.put("birthdate", List.of(this.account.getBirthdate().toString()));
        attributes.put("gender", List.of(accountToUpdate.getGender().toString()));
        this.userRepresentation.setAttributes(attributes);
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(accountToUpdate.getPassword());
        this.userRepresentation.setCredentials(List.of(credential));

        this.convertedAccount.setName(accountToUpdate.getName());
        this.convertedAccount.setSurname(accountToUpdate.getSurname());
        this.convertedAccount.setGender(Account.GenderEnum.fromValue(accountToUpdate.getGender().toString()));


        when(this.keycloakService.updateUser(anyString(), any(AccountPatch.class))).thenReturn(this.userRepresentation);
        when(this.userRepresentationToAccountConverter.convert(any(UserRepresentation.class))).thenReturn(this.convertedAccount);

        Account updatedAccount = this.accountsService.update(this.account.getId(), accountToUpdate);


        assertEquals(this.account.getId(), updatedAccount.getId());
        assertEquals(accountToUpdate.getName(), updatedAccount.getName());
        assertEquals(accountToUpdate.getSurname(), updatedAccount.getSurname());
        assertEquals(accountToUpdate.getGender().toString(), updatedAccount.getGender().toString());
        assertNull(updatedAccount.getPassword());
        log.info(updatedAccount.toString());
        verify(this.keycloakService, times(1)).updateUser(anyString(), any(AccountPatch.class));
        verify(this.userRepresentationToAccountConverter, times(1)).convert(any(UserRepresentation.class));
    }


    // ACTIVE ACCOUNT

    @Test
    void testActive_Success(){
        when(this.keycloakService.validateEmail(anyString(), anyString())).thenReturn(true);

        this.accountsService.active(this.account.getId(), UUID.randomUUID().toString());

        verify(this.keycloakService, times(1)).validateEmail(anyString(), anyString());

    }


    @Test
    void testActive_ErrorOn_ValidationCode_NotActivated_Failed(){
        when(this.keycloakService.validateEmail(anyString(), anyString())).thenReturn(false);

        assertThrows(AccountNotActivedException.class,
                ()->{
                    this.accountsService.active(this.account.getId(), UUID.randomUUID().toString());
                }
        );

        verify(this.keycloakService, times(1)).validateEmail(anyString(), anyString());

    }


}