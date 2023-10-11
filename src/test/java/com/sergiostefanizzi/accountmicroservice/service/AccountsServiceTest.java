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

        this.newAccountJpa.setName("Pinco");
        this.newAccountJpa.setSurname("Pallino");

        validationCode = UUID.randomUUID();
        this.newAccountJpa.setValidationCode(validationCode.toString());

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

        Account savedAccount = this.accountsService.save(newAccount);

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
            this.accountsService.save(newAccount)
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
/*
    @Test
    void testUpdate_Success(){
        AccountJpa accountJpaToUpdate = new AccountJpa(
                "mario.rossi@gmail.com",
                "Mario",
                "Rossi",
                LocalDate.of(1990,4,4),
                AccountJpa.Gender.MALE,
                "5434543jhkhjjh"
        );
        accountJpaToUpdate.setId(accountId);

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
                accountJpaToUpdate.getEmail(),
                accountJpaToUpdate.getBirthdate(),
                Account.GenderEnum.valueOf(accountToUpdate.getGender().toString()),
                accountToUpdate.getPassword()
        );
        convertedUpdatedAccount.setId(accountId);
        convertedUpdatedAccount.setName(accountToUpdate.getName());
        convertedUpdatedAccount.setSurname(accountToUpdate.getSurname());

        when(this.accountsRepository.findById(accountId)).thenReturn(Optional.of(accountJpaToUpdate));
        when(this.accountsRepository.save(accountJpaToUpdate)).thenReturn(accountJpaToUpdate);
        when(this.accountToJpaConverter.convertBack(accountJpaToUpdate)).thenReturn(convertedUpdatedAccount);

        Account updatedAccount = this.accountsService.update(accountId, accountToUpdate);

        log.info("updated at "+accountJpaToUpdate.getUpdatedAt());

        assertNotNull(accountJpaToUpdate.getUpdatedAt());
        assertEquals(accountId, updatedAccount.getId());
        assertEquals(accountToUpdate.getName(), updatedAccount.getName());
        assertEquals(accountToUpdate.getSurname(), updatedAccount.getSurname());
        assertEquals(accountToUpdate.getGender().toString(), updatedAccount.getGender().toString());
        assertEquals(accountToUpdate.getPassword(), updatedAccount.getPassword());
        verify(this.accountsRepository, times(1)).findById(accountId);
        verify(this.accountsRepository, times(1)).save(accountJpaToUpdate);
        verify(this.accountToJpaConverter, times(1)).convertBack(accountJpaToUpdate);
    }


    @Test
    void testUpdate_AccountNotFound_Failed(){
        when(this.accountsRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,() -> {
            this.accountsService.update(accountId, any(AccountPatch.class));
        });
        verify(this.accountsRepository, times(1)).findById(accountId);
        verify(this.accountsRepository, times(0)).save(any(AccountJpa.class));
    }


    @Test
    void testUpdate_RemovedAccount_Failed(){
        AccountJpa removedAccountJpa = new AccountJpa(
                "mario.rossi@gmail.com",
                "Mario",
                "Rossi",
                LocalDate.of(1990,4,4),
                AccountJpa.Gender.MALE,
                "5434543jhkhjjh!"
        );
        removedAccountJpa.setId(accountId);
        removedAccountJpa.setDeletedAt(Timestamp.valueOf(LocalDateTime.of(2022,5,4,12,30,15)));

        when(this.accountsRepository.findById(accountId)).thenReturn(Optional.of(removedAccountJpa));

        assertThrows(AccountNotFoundException.class,() -> {
            this.accountsService.update(accountId, any(AccountPatch.class));
        });
        verify(this.accountsRepository, times(1)).findById(accountId);
        verify(this.accountsRepository, times(0)).save(any(AccountJpa.class));
    }

    // ACTIVE ACCOUNT

    @Test
    void testActive_Success(){
        AccountJpa accountJpaToActive = new AccountJpa(
                newAccountJpa.getEmail(),
                newAccountJpa.getName(),
                newAccountJpa.getSurname(),
                newAccountJpa.getBirthdate(),
                newAccountJpa.getGender(),
                newAccountJpa.getPassword());
        accountJpaToActive.setValidationCode(validationCode.toString());
        accountJpaToActive.setId(accountId);

        log.info(accountJpaToActive.getValidatedAt() == null ? "NULL" : accountJpaToActive.getValidatedAt().toString());

        when(this.accountsRepository.findById(accountId)).thenReturn(Optional.of(accountJpaToActive));

        this.accountsService.active(accountId, validationCode.toString());

        log.info(accountJpaToActive.getValidatedAt() == null ? "NULL" : accountJpaToActive.getValidatedAt().toString());


        assertEquals(validationCode.toString(), accountJpaToActive.getValidationCode());
        verify(this.accountsRepository, times(1)).findById(accountId);
        verify(this.accountsRepository, times(1)).save(accountJpaToActive);

    }

    @Test
    void testActive_AlreadyValidated_Success(){
        AccountJpa accountJpaToActive = new AccountJpa(
                newAccountJpa.getEmail(),
                newAccountJpa.getName(),
                newAccountJpa.getSurname(),
                newAccountJpa.getBirthdate(),
                newAccountJpa.getGender(),
                newAccountJpa.getPassword());
        accountJpaToActive.setValidationCode(validationCode.toString());
        accountJpaToActive.setId(accountId);
        accountJpaToActive.setValidatedAt(Timestamp.valueOf(LocalDateTime.of(2022,1,2,12,15,0)));

        log.info(accountJpaToActive.getValidatedAt() == null ? "NULL" : accountJpaToActive.getValidatedAt().toString());

        when(this.accountsRepository.findById(accountId)).thenReturn(Optional.of(accountJpaToActive));

        this.accountsService.active(accountId, validationCode.toString());

        log.info(accountJpaToActive.getValidatedAt() == null ? "NULL" : accountJpaToActive.getValidatedAt().toString());


        assertEquals(validationCode.toString(), accountJpaToActive.getValidationCode());
        verify(this.accountsRepository, times(1)).findById(accountId);
        verify(this.accountsRepository, times(0)).save(accountJpaToActive);

    }


    @Test
    void testActive_NotFound_NotActivated_Failed(){
        when(this.accountsRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotActivedException.class,
                ()->{
                    this.accountsService.active(accountId, validationCode.toString());
                }
        );

        verify(this.accountsRepository, times(1)).findById(accountId);
        verify(this.accountsRepository, times(0)).save(any(AccountJpa.class));

    }

    @Test
    void testActive_ErrorOn_ValidationCode_NotActivated_Failed(){
        AccountJpa accountJpaToActive = new AccountJpa(
                newAccountJpa.getEmail(),
                newAccountJpa.getName(),
                newAccountJpa.getSurname(),
                newAccountJpa.getBirthdate(),
                newAccountJpa.getGender(),
                newAccountJpa.getPassword());

        accountJpaToActive.setValidationCode(validationCode.toString());
        accountJpaToActive.setId(accountId);

        UUID newValidationCode = UUID.randomUUID();

        when(this.accountsRepository.findById(accountId)).thenReturn(Optional.of(accountJpaToActive));

        assertThrows(AccountNotActivedException.class,
                ()->{
                    this.accountsService.active(accountId, newValidationCode.toString());
                }
        );

        verify(this.accountsRepository, times(1)).findById(accountId);
        verify(this.accountsRepository, times(0)).save(any(AccountJpa.class));

    }

     */

}