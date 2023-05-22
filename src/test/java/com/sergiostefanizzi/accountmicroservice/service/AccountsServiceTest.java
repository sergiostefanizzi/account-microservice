package com.sergiostefanizzi.accountmicroservice.service;

import com.sergiostefanizzi.accountmicroservice.controller.converter.AccountsToJpaConverter;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AccountAlreadyCreatedException;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AccountNotFoundException;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.ValidationCodeNotValidException;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.AccountPatch;
import com.sergiostefanizzi.accountmicroservice.repository.AccountsRepository;
import com.sergiostefanizzi.accountmicroservice.repository.model.AccountJpa;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
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
    UUID validationCode;
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

        validationCode = UUID.randomUUID();
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
        AccountJpa accountToRemove = new AccountJpa(
                savedAccountJpa.getEmail(),
                savedAccountJpa.getName(),
                savedAccountJpa.getSurname(),
                savedAccountJpa.getBirthdate(),
                savedAccountJpa.getGender(),
                savedAccountJpa.getPassword()
        );
        accountToRemove.setId(savedAccountJpa.getId());
        log.info(accountToRemove.getDeletedAt() == null ? "deletedAt is NULL" : "deletedAt is "+accountToRemove.getDeletedAt().toString());

        when(this.accountsRepository.findById(accountId)).thenReturn(Optional.of(accountToRemove));

        this.accountsService.remove(accountToRemove.getId());

        log.info(accountToRemove.getDeletedAt() == null ? "deletedAt is NULL" : "deletedAt is "+accountToRemove.getDeletedAt().toString());
        assertNotNull(accountToRemove.getDeletedAt());
        verify(this.accountsRepository, times(1)).findById(accountId);
        verify(this.accountsRepository, times(1)).save(accountToRemove);
    }

    @Test
    void testRemove_AccountNotFound_Failed(){
        when(this.accountsRepository.findById(accountId)).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class,
                () -> this.accountsService.remove(accountId)
        );

        verify(this.accountsRepository, times(1)).findById(accountId);
        verify(this.accountsRepository, times(0)).save(any(AccountJpa.class));
    }

    @Test
    void testRemove_AccountAlreadyRemoved_Failed(){
        AccountJpa accountToRemove = new AccountJpa(
                savedAccountJpa.getEmail(),
                savedAccountJpa.getName(),
                savedAccountJpa.getSurname(),
                savedAccountJpa.getBirthdate(),
                savedAccountJpa.getGender(),
                savedAccountJpa.getPassword()
        );
        accountToRemove.setId(savedAccountJpa.getId());
        accountToRemove.setDeletedAt(Timestamp.valueOf(LocalDateTime.now()));

        when(this.accountsRepository.findById(accountId)).thenReturn(Optional.of(accountToRemove));

        assertThrows(AccountNotFoundException.class,
                () -> this.accountsService.remove(accountId)
        );

        verify(this.accountsRepository, times(1)).findById(accountId);
        verify(this.accountsRepository, times(0)).save(any(AccountJpa.class));
    }

    // UPDATE ACCOUNT
    //TODO: modificare i test di update secondo la modifica in remove
    @Test
    void testUpdate_Success(){
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
    void testUpdate_Name_Success(){
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
    void testUpdate_Surname_Success(){
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
    void testUpdate_Gender_Success(){
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
    void testUpdate_Password_Success(){
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
                oldAccountJpa.getEmail(),
                oldAccountJpa.getName(),
                oldAccountJpa.getSurname(),
                oldAccountJpa.getBirthdate(),
                oldAccountJpa.getGender(),
                oldAccountJpa.getPassword()
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
        log.info(savedAccountJpa.getValidatedAt() == null ? "NULL" : savedAccountJpa.getValidatedAt().toString());
        Timestamp validation = savedAccountJpa.getValidatedAt();
        when(this.accountsRepository.findById(accountId)).thenReturn(Optional.of(savedAccountJpa));


        this.accountsService.active(accountId, validationCode.toString());

        log.info(savedAccountJpa.getValidatedAt() == null ? "NULL" : savedAccountJpa.getValidatedAt().toString());


        assertNotEquals(validation, savedAccountJpa.getValidatedAt());
        assertEquals(validationCode.toString(), savedAccountJpa.getValidationCode());
        verify(this.accountsRepository, times(1)).findById(accountId);
        verify(this.accountsRepository, times(1)).save(savedAccountJpa);

    }

    @Test
    void testActive_AlreadyValidated_Success(){
        savedAccountJpa.setValidatedAt(Timestamp.valueOf(LocalDateTime.of(2022,1,2,12,15,0)));
        log.info(savedAccountJpa.getValidatedAt() == null ? "NULL" : savedAccountJpa.getValidatedAt().toString());
        Timestamp validation = savedAccountJpa.getValidatedAt();
        when(this.accountsRepository.findById(accountId)).thenReturn(Optional.of(savedAccountJpa));


        this.accountsService.active(accountId, validationCode.toString());

        log.info(savedAccountJpa.getValidatedAt() == null ? "NULL" : savedAccountJpa.getValidatedAt().toString());

        assertEquals(validation, savedAccountJpa.getValidatedAt());
        assertEquals(validationCode.toString(), savedAccountJpa.getValidationCode());
        verify(this.accountsRepository, times(1)).findById(accountId);
        //dato che validatedAt è diverso da null, quindi l'account è già stato validato, save non sarà chiamata
        verify(this.accountsRepository, times(0)).save(savedAccountJpa);

    }

    @Test
    void testActive_AccountNotFound_Failed(){
        when(this.accountsRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                ()->{
                    this.accountsService.active(accountId, validationCode.toString());
                }
        );

        verify(this.accountsRepository, times(1)).findById(accountId);
        verify(this.accountsRepository, times(0)).save(any(AccountJpa.class));

    }

    @Test
    void testActive_ValidationCode_NotValid_Failed(){
        UUID invalidCode = UUID.randomUUID();
        when(this.accountsRepository.findById(accountId)).thenReturn(Optional.ofNullable(savedAccountJpa));

        assertNotEquals(invalidCode.toString(), savedAccountJpa.getValidationCode());
        assertThrows(ValidationCodeNotValidException.class,
                ()->{
                    this.accountsService.active(accountId, invalidCode.toString());
                }
        );
        verify(this.accountsRepository, times(1)).findById(accountId);
        verify(this.accountsRepository, times(0)).save(any(AccountJpa.class));

    }

}