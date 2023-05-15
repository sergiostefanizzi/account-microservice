package com.sergiostefanizzi.accountmicroservice.service;

import com.sergiostefanizzi.accountmicroservice.controller.converter.AccountsToJpaConverter;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AccountAlreadyCreatedException;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AccountNotFoundException;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.AccountPatch;
import com.sergiostefanizzi.accountmicroservice.repository.AccountsRepository;
import com.sergiostefanizzi.accountmicroservice.repository.model.AccountJpa;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountsServiceTest {
    @Mock
    private AccountsRepository accountsRepository;
    @Mock
    private AccountsToJpaConverter accountsToJpaConverter;
    @InjectMocks
    private AccountsService accountsService;

    Account newAccount;
    Account convertedSavedAccount;
    AccountJpa newAccountJpa;
    AccountJpa savedAccountJpa;
    AccountJpa oldAccountJpa;

    AccountPatch accountToUpdate;
    Long accountId = 1L;
    @BeforeEach
    void setUp() {
        newAccount = new Account("mario.rossi@gmail.com",
                LocalDate.of(1990,4,4),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        newAccount.setName("Mario");
        newAccount.setSurname("Rossi");

        newAccountJpa = new AccountJpa(newAccount.getEmail(),
                newAccount.getName(),
                newAccount.getSurname(),
                newAccount.getBirthdate(),
                AccountJpa.Gender.valueOf(newAccount.getGender().toString()),
                newAccount.getPassword());

        UUID validationCode = UUID.randomUUID();
        newAccountJpa.setValidationCode(validationCode.toString());

        savedAccountJpa = new AccountJpa(newAccountJpa.getEmail(),
                newAccountJpa.getName(),
                newAccountJpa.getSurname(),
                newAccountJpa.getBirthdate(),
                newAccountJpa.getGender(),
                newAccountJpa.getPassword());
        savedAccountJpa.setValidationCode(validationCode.toString());
        savedAccountJpa.setId(accountId);

        convertedSavedAccount = new Account(savedAccountJpa.getEmail(),
                savedAccountJpa.getBirthdate(),
                Account.GenderEnum.valueOf(savedAccountJpa.getGender().toString()),
                savedAccountJpa.getPassword());
        convertedSavedAccount.setId(savedAccountJpa.getId());
        convertedSavedAccount.setName(savedAccountJpa.getName());
        convertedSavedAccount.setSurname(savedAccountJpa.getSurname());

        oldAccountJpa = new AccountJpa(
                "mario.rossi@gmail.com",
                "Mario",
                "Rossi",
                LocalDate.of(1990,4,4),
                AccountJpa.Gender.MALE,
                "5434543jhkhjjh"
        );
        oldAccountJpa.setId(accountId);
    }

    @AfterEach
    void tearDown() {
    }
    // SAVE ACCOUNT
    @Test
    void testSave_Success() {
        when(this.accountsRepository.findByEmail(newAccount.getEmail())).thenReturn(Optional.empty());
        when(this.accountsToJpaConverter.convert(newAccount)).thenReturn(newAccountJpa);
        when(this.accountsRepository.save(newAccountJpa)).thenReturn(savedAccountJpa);
        when(this.accountsToJpaConverter.convertBack(savedAccountJpa)).thenReturn(convertedSavedAccount);

        Account savedAccount = this.accountsService.save(newAccount);

        assertEquals(accountId, savedAccount.getId());
        assertEquals(newAccount.getEmail(), savedAccount.getEmail());
        assertEquals(newAccount.getName(), savedAccount.getName());
        assertEquals(newAccount.getSurname(), savedAccount.getSurname());
        assertEquals(newAccount.getBirthdate(), savedAccount.getBirthdate());
        assertEquals(newAccount.getGender(), savedAccount.getGender());
        assertEquals(newAccount.getPassword(), savedAccount.getPassword());
        verify(this.accountsRepository, times(1)).findByEmail(newAccount.getEmail());
        verify(this.accountsRepository, times(1)).save(newAccountJpa);
    }


    @Test
    void testSaveAccount_Failed(){
        when(this.accountsRepository.findByEmail(newAccount.getEmail())).thenReturn(Optional.ofNullable(savedAccountJpa));

        assertThrows(AccountAlreadyCreatedException.class, () ->
            this.accountsService.save(newAccount)
        );

        verify(this.accountsRepository, times(1)).findByEmail(newAccount.getEmail());
        verify(this.accountsRepository, times(0)).save(Mockito.any(AccountJpa.class));

    }

    // DELETE ACCOUNT
    @Test
    void testRemove_Success(){
        when(this.accountsRepository.findById(accountId)).thenReturn(Optional.ofNullable(savedAccountJpa));
        doNothing().when(this.accountsRepository).deleteById(accountId);
        this.accountsService.remove(savedAccountJpa.getId());
        verify(this.accountsRepository, times(1)).deleteById(accountId);
    }

    @Test
    void testRemove_Failed(){
        when(this.accountsRepository.findById(accountId)).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class,
                () -> this.accountsService.remove(accountId)
        );

        verify(this.accountsRepository, times(0)).deleteById(newAccountJpa.getId());
    }

    // UPDATE ACCOUNT

    @Test
    void  testUpdate_Success(){
        String newName = "Giuseppe";
        String newSurname = "Verdi";
        AccountPatch.GenderEnum newGender = AccountPatch.GenderEnum.FEMALE;
        String newPassword = "43hg434j5g4";

        AccountPatch accountToUpdate = new AccountPatch();
        accountToUpdate.setName(newName);
        accountToUpdate.setSurname(newSurname);
        accountToUpdate.setGender(newGender);
        accountToUpdate.setPassword(newPassword);

        AccountJpa updatedAccountJpa = new AccountJpa(
                oldAccountJpa.getEmail(),
                accountToUpdate.getName(),
                accountToUpdate.getSurname(),
                oldAccountJpa.getBirthdate(),
                AccountJpa.Gender.valueOf(accountToUpdate.getGender().toString()),
                accountToUpdate.getPassword()
        );
        updatedAccountJpa.setId(accountId);

        Account convertedUpdatedAccount = new Account(
                updatedAccountJpa.getEmail(),
                updatedAccountJpa.getBirthdate(),
                Account.GenderEnum.valueOf(updatedAccountJpa.getGender().toString()),
                updatedAccountJpa.getPassword()
        );
        convertedUpdatedAccount.setId(accountId);
        convertedUpdatedAccount.setName(updatedAccountJpa.getName());
        convertedUpdatedAccount.setSurname(updatedAccountJpa.getSurname());

        when(this.accountsRepository.findById(accountId)).thenReturn(Optional.ofNullable(oldAccountJpa));
        when(this.accountsRepository.save(any(AccountJpa.class))).thenReturn(updatedAccountJpa);
        when(this.accountsToJpaConverter.convertBack(updatedAccountJpa)).thenReturn(convertedUpdatedAccount);

        Account updatedAccount = this.accountsService.update(accountId, accountToUpdate);

        assertEquals(accountId, updatedAccount.getId());
        assertEquals(accountToUpdate.getName(), updatedAccount.getName());
        assertEquals(accountToUpdate.getSurname(), updatedAccount.getSurname());
        assertEquals(accountToUpdate.getGender().toString(), updatedAccount.getGender().toString());
        assertEquals(accountToUpdate.getPassword(), updatedAccount.getPassword());
        verify(this.accountsRepository, times(1)).findById(accountId);
        verify(this.accountsRepository, times(1)).save(any(AccountJpa.class));
    }

    @Test
    void  testUpdate_Name_Success(){
        String newName = "Giuseppe";

        AccountPatch accountToUpdate = new AccountPatch();
        accountToUpdate.setName(newName);

        AccountJpa updatedAccountJpa = new AccountJpa(
                oldAccountJpa.getEmail(),
                accountToUpdate.getName(),
                oldAccountJpa.getSurname(),
                oldAccountJpa.getBirthdate(),
                oldAccountJpa.getGender(),
                oldAccountJpa.getPassword()
        );
        updatedAccountJpa.setId(accountId);

        Account convertedUpdatedAccount = new Account(
                updatedAccountJpa.getEmail(),
                updatedAccountJpa.getBirthdate(),
                Account.GenderEnum.valueOf(updatedAccountJpa.getGender().toString()),
                updatedAccountJpa.getPassword()
        );
        convertedUpdatedAccount.setId(accountId);
        convertedUpdatedAccount.setName(updatedAccountJpa.getName());
        convertedUpdatedAccount.setSurname(updatedAccountJpa.getSurname());

        when(this.accountsRepository.findById(accountId)).thenReturn(Optional.ofNullable(oldAccountJpa));
        when(this.accountsRepository.save(any(AccountJpa.class))).thenReturn(updatedAccountJpa);
        when(this.accountsToJpaConverter.convertBack(updatedAccountJpa)).thenReturn(convertedUpdatedAccount);

        Account updatedAccount = this.accountsService.update(accountId, accountToUpdate);

        assertEquals(accountId, updatedAccount.getId());
        assertEquals(accountToUpdate.getName(), updatedAccount.getName());
        assertEquals(oldAccountJpa.getSurname(), updatedAccount.getSurname());
        assertEquals(oldAccountJpa.getGender().toString(), updatedAccount.getGender().toString());
        assertEquals(oldAccountJpa.getPassword(), updatedAccount.getPassword());
        verify(this.accountsRepository, times(1)).findById(accountId);
        verify(this.accountsRepository, times(1)).save(any(AccountJpa.class));
    }

    @Test
    void  testUpdate_Surname_Success(){
        String newSurname = "Verdi";

        AccountPatch accountToUpdate = new AccountPatch();
        accountToUpdate.setSurname(newSurname);

        AccountJpa updatedAccountJpa = new AccountJpa(
                oldAccountJpa.getEmail(),
                oldAccountJpa.getName(),
                accountToUpdate.getSurname(),
                oldAccountJpa.getBirthdate(),
                oldAccountJpa.getGender(),
                oldAccountJpa.getPassword()
        );
        updatedAccountJpa.setId(accountId);

        Account convertedUpdatedAccount = new Account(
                updatedAccountJpa.getEmail(),
                updatedAccountJpa.getBirthdate(),
                Account.GenderEnum.valueOf(updatedAccountJpa.getGender().toString()),
                updatedAccountJpa.getPassword()
        );
        convertedUpdatedAccount.setId(accountId);
        convertedUpdatedAccount.setName(updatedAccountJpa.getName());
        convertedUpdatedAccount.setSurname(updatedAccountJpa.getSurname());

        when(this.accountsRepository.findById(accountId)).thenReturn(Optional.ofNullable(oldAccountJpa));
        when(this.accountsRepository.save(any(AccountJpa.class))).thenReturn(updatedAccountJpa);
        when(this.accountsToJpaConverter.convertBack(updatedAccountJpa)).thenReturn(convertedUpdatedAccount);

        Account updatedAccount = this.accountsService.update(accountId, accountToUpdate);

        assertEquals(accountId, updatedAccount.getId());
        assertEquals(oldAccountJpa.getName(), updatedAccount.getName());
        assertEquals(accountToUpdate.getSurname(), updatedAccount.getSurname());
        assertEquals(oldAccountJpa.getGender().toString(), updatedAccount.getGender().toString());
        assertEquals(oldAccountJpa.getPassword(), updatedAccount.getPassword());
        verify(this.accountsRepository, times(1)).findById(accountId);
        verify(this.accountsRepository, times(1)).save(any(AccountJpa.class));
    }

    @Test
    void  testUpdate_Gender_Success(){
        AccountPatch.GenderEnum newGender = AccountPatch.GenderEnum.FEMALE;
        AccountPatch accountToUpdate = new AccountPatch();
        accountToUpdate.setGender(newGender);

        AccountJpa updatedAccountJpa = new AccountJpa(
                oldAccountJpa.getEmail(),
                oldAccountJpa.getName(),
                oldAccountJpa.getSurname(),
                oldAccountJpa.getBirthdate(),
                AccountJpa.Gender.valueOf(accountToUpdate.getGender().toString()),
                oldAccountJpa.getPassword()
        );
        updatedAccountJpa.setId(accountId);

        Account convertedUpdatedAccount = new Account(
                updatedAccountJpa.getEmail(),
                updatedAccountJpa.getBirthdate(),
                Account.GenderEnum.valueOf(updatedAccountJpa.getGender().toString()),
                updatedAccountJpa.getPassword()
        );
        convertedUpdatedAccount.setId(accountId);
        convertedUpdatedAccount.setName(updatedAccountJpa.getName());
        convertedUpdatedAccount.setSurname(updatedAccountJpa.getSurname());

        when(this.accountsRepository.findById(accountId)).thenReturn(Optional.ofNullable(oldAccountJpa));
        when(this.accountsRepository.save(any(AccountJpa.class))).thenReturn(updatedAccountJpa);
        when(this.accountsToJpaConverter.convertBack(updatedAccountJpa)).thenReturn(convertedUpdatedAccount);

        Account updatedAccount = this.accountsService.update(accountId, accountToUpdate);

        assertEquals(accountId, updatedAccount.getId());
        assertEquals(oldAccountJpa.getName(), updatedAccount.getName());
        assertEquals(oldAccountJpa.getSurname(), updatedAccount.getSurname());
        assertEquals(accountToUpdate.getGender().toString(), updatedAccount.getGender().toString());
        assertEquals(oldAccountJpa.getPassword(), updatedAccount.getPassword());
        verify(this.accountsRepository, times(1)).findById(accountId);
        verify(this.accountsRepository, times(1)).save(any(AccountJpa.class));
    }

    @Test
    void  testUpdate_Password_Success(){
        String newPassword = "43hg434j5g4!";

        AccountPatch accountToUpdate = new AccountPatch();
        accountToUpdate.setPassword(newPassword);

        AccountJpa updatedAccountJpa = new AccountJpa(
                oldAccountJpa.getEmail(),
                oldAccountJpa.getName(),
                oldAccountJpa.getSurname(),
                oldAccountJpa.getBirthdate(),
                oldAccountJpa.getGender(),
                accountToUpdate.getPassword()
        );
        updatedAccountJpa.setId(accountId);

        Account convertedUpdatedAccount = new Account(
                updatedAccountJpa.getEmail(),
                updatedAccountJpa.getBirthdate(),
                Account.GenderEnum.valueOf(updatedAccountJpa.getGender().toString()),
                updatedAccountJpa.getPassword()
        );
        convertedUpdatedAccount.setId(accountId);
        convertedUpdatedAccount.setName(updatedAccountJpa.getName());
        convertedUpdatedAccount.setSurname(updatedAccountJpa.getSurname());

        when(this.accountsRepository.findById(accountId)).thenReturn(Optional.ofNullable(oldAccountJpa));
        when(this.accountsRepository.save(any(AccountJpa.class))).thenReturn(updatedAccountJpa);
        when(this.accountsToJpaConverter.convertBack(updatedAccountJpa)).thenReturn(convertedUpdatedAccount);

        Account updatedAccount = this.accountsService.update(accountId, accountToUpdate);

        assertEquals(accountId, updatedAccount.getId());
        assertEquals(oldAccountJpa.getName(), updatedAccount.getName());
        assertEquals(oldAccountJpa.getSurname(), updatedAccount.getSurname());
        assertEquals(oldAccountJpa.getGender().toString(), updatedAccount.getGender().toString());
        assertEquals(accountToUpdate.getPassword(), updatedAccount.getPassword());
        verify(this.accountsRepository, times(1)).findById(accountId);
        verify(this.accountsRepository, times(1)).save(any(AccountJpa.class));
    }

    @Test
    void  testUpdate_NotFound_Failed(){
        String newName = "Giuseppe";
        String newSurname = "Verdi";
        AccountPatch.GenderEnum newGender = AccountPatch.GenderEnum.FEMALE;
        String newPassword = "43hg434j5g4!";

        AccountPatch accountToUpdate = new AccountPatch();
        accountToUpdate.setName(newName);
        accountToUpdate.setSurname(newSurname);
        accountToUpdate.setGender(newGender);
        accountToUpdate.setPassword(newPassword);

        when(this.accountsRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,() -> {
            this.accountsService.update(accountId, accountToUpdate);
        });
        verify(this.accountsRepository, times(1)).findById(accountId);
        verify(this.accountsRepository, times(0)).save(any(AccountJpa.class));
    }
}