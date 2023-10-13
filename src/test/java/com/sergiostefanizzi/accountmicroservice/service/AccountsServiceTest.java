package com.sergiostefanizzi.accountmicroservice.service;

import com.sergiostefanizzi.accountmicroservice.controller.converter.AccountToJpaConverter;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AccountAlreadyCreatedException;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AccountNotActivedException;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AccountNotFoundException;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.AccountPatch;
import com.sergiostefanizzi.accountmicroservice.repository.AccountsRepository;
import com.sergiostefanizzi.accountmicroservice.model.AccountJpa;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@Slf4j
class AccountsServiceTest {
    @Mock
    private AccountsRepository accountsRepository;
    @Mock
    private AccountToJpaConverter accountToJpaConverter;
    @InjectMocks
    private AccountsService accountsService;

    private Account newAccount;
    private AccountJpa newAccountJpa;
    private Account convertedAccount;

    private AccountJpa savedAccountJpa;

    UUID validationCode;
    Long accountId = 1L;
    @BeforeEach
    void setUp() {
        this.newAccount = new Account("pinco.pallino@gmail.com",
                LocalDate.of(1990,4,4),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        this.newAccount.setName("Pinco");
        this.newAccount.setSurname("Pallino");

        this.newAccountJpa = new AccountJpa(
                this.newAccount.getEmail(),
                this.newAccount.getBirthdate(),
                this.newAccount.getGender(),
                this.newAccount.getPassword()
                );
        this.newAccountJpa.setName(this.newAccount.getName());
        this.newAccountJpa.setSurname(this.newAccount.getSurname());
        this.newAccountJpa.setValidationCode(UUID.randomUUID().toString());

        this.convertedAccount = new Account(this.newAccountJpa.getEmail(),
                this.newAccountJpa.getBirthdate(),
                this.newAccountJpa.getGender(),
                null);
        this.convertedAccount.setId(101L);
        this.convertedAccount.setName(this.newAccountJpa.getName());
        this.convertedAccount.setSurname(this.newAccountJpa.getSurname());

        this.savedAccountJpa = new AccountJpa(
                "mario.bros@gmail.com",
                LocalDate.of(1990,4,4),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!"
        );

        this.savedAccountJpa.setName("Mario");
        this.savedAccountJpa.setSurname("Bros");
        this.savedAccountJpa.setId(102L);
        this.savedAccountJpa.setValidationCode(UUID.randomUUID().toString());
        this.savedAccountJpa.setValidatedAt(LocalDateTime.now().minusDays(1));
    }

    @AfterEach
    void tearDown() {
    }

    // SAVE ACCOUNT
    @Test
    void testSave_Success() {
        when(this.accountsRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(this.accountToJpaConverter.convert(any(Account.class))).thenReturn(this.newAccountJpa);
        when(this.accountsRepository.save(any(AccountJpa.class))).thenReturn(this.newAccountJpa);
        when(this.accountToJpaConverter.convertBack(any(AccountJpa.class))).thenReturn(this.convertedAccount);

        Account savedAccount = this.accountsService.save(this.newAccount);

        assertEquals(this.convertedAccount, savedAccount);
        verify(this.accountsRepository, times(1)).findByEmail(anyString());
        verify(this.accountsRepository, times(1)).save(any(AccountJpa.class));
        verify(this.accountToJpaConverter, times(1)).convert(any(Account.class));
        verify(this.accountToJpaConverter, times(1)).convertBack(any(AccountJpa.class));

        log.info(savedAccount.toString());
    }


    @Test
    void testSaveAccount_Failed(){
        when(this.accountsRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(savedAccountJpa));

        assertThrows(AccountAlreadyCreatedException.class, () ->
            this.accountsService.save(this.newAccount)
        );

        verify(this.accountsRepository, times(1)).findByEmail(anyString());
        verify(this.accountsRepository, times(0)).save(any(AccountJpa.class));

    }

    // DELETE ACCOUNT
    @Test
    void testRemove_Success(){
        when(this.accountsRepository.getReferenceById(anyLong())).thenReturn(this.savedAccountJpa);

        this.accountsService.remove(this.savedAccountJpa.getId());

        log.info("Deleted At -> "+this.savedAccountJpa.getDeletedAt());
        assertNotNull(this.savedAccountJpa.getDeletedAt());
        verify(this.accountsRepository, times(1)).getReferenceById(anyLong());
        verify(this.accountsRepository, times(1)).save(any(AccountJpa.class));
    }



    // UPDATE ACCOUNT

    @Test
    void testUpdate_Success(){

        String newName = "Giuseppe";
        String newSurname = "Verdi";
        AccountPatch.GenderEnum newGender = AccountPatch.GenderEnum.FEMALE;
        String newPassword = "43hg434j5g4!";

        AccountPatch accountToUpdate = new AccountPatch();
        accountToUpdate.setName(newName);
        accountToUpdate.setSurname(newSurname);
        accountToUpdate.setGender(newGender);
        accountToUpdate.setPassword(newPassword);

        Account convertedUpdatedAccount = new Account(
                this.savedAccountJpa.getEmail(),
                this.savedAccountJpa.getBirthdate(),
                Account.GenderEnum.valueOf(accountToUpdate.getGender().toString()),
                accountToUpdate.getPassword()
        );
        convertedUpdatedAccount.setId(this.savedAccountJpa.getId());
        convertedUpdatedAccount.setName(accountToUpdate.getName());
        convertedUpdatedAccount.setSurname(accountToUpdate.getSurname());

        when(this.accountsRepository.getReferenceById(anyLong())).thenReturn(this.savedAccountJpa);
        when(this.accountsRepository.save(any(AccountJpa.class))).thenReturn(this.savedAccountJpa);
        when(this.accountToJpaConverter.convertBack(any(AccountJpa.class))).thenReturn(convertedUpdatedAccount);

        Account updatedAccount = this.accountsService.update(this.savedAccountJpa.getId(), accountToUpdate);

        assertNotNull(this.savedAccountJpa.getUpdatedAt());
        log.info("updated at "+this.savedAccountJpa.getUpdatedAt());
        assertEquals(this.savedAccountJpa.getId(), updatedAccount.getId());
        assertEquals(accountToUpdate.getName(), updatedAccount.getName());
        assertEquals(accountToUpdate.getSurname(), updatedAccount.getSurname());
        assertEquals(accountToUpdate.getGender().toString(), updatedAccount.getGender().toString());
        assertEquals(accountToUpdate.getPassword(), updatedAccount.getPassword());
        verify(this.accountsRepository, times(1)).getReferenceById(anyLong());
        verify(this.accountsRepository, times(1)).save(any(AccountJpa.class));
        verify(this.accountToJpaConverter, times(1)).convertBack(any(AccountJpa.class));
    }


    // ACTIVE ACCOUNT

    @Test
    void testActive_Success(){
        this.newAccountJpa.setId(101L);
        when(this.accountsRepository.getReferenceById(anyLong())).thenReturn(this.newAccountJpa);

        this.accountsService.active(this.newAccountJpa.getId(), this.newAccountJpa.getValidationCode());

        verify(this.accountsRepository, times(1)).getReferenceById(anyLong());
        verify(this.accountsRepository, times(1)).save(any(AccountJpa.class));

    }


    @Test
    void testActive_ErrorOn_ValidationCode_NotActivated_Failed(){
        this.newAccountJpa.setId(101L);
        when(this.accountsRepository.getReferenceById(anyLong())).thenReturn(this.newAccountJpa);

        assertThrows(AccountNotActivedException.class,
                ()->{
                    this.accountsService.active(this.newAccountJpa.getId(), UUID.randomUUID().toString());
                }
        );
        verify(this.accountsRepository, times(1)).getReferenceById(anyLong());
        verify(this.accountsRepository, times(0)).save(any(AccountJpa.class));


    }



}