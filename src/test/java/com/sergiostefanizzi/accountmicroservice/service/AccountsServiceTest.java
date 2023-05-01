package com.sergiostefanizzi.accountmicroservice.service;

import com.sergiostefanizzi.accountmicroservice.controller.converter.AccountsToJpaConverter;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AccountAlreadyCreated;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AccountIdNotFound;
import com.sergiostefanizzi.accountmicroservice.model.Account;
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
    AccountJpa newAccountJpa;
    AccountJpa savedAccountJpa;
    Account convertedSavedAccount;
    @BeforeEach
    void setUp() {
        newAccount = new Account("mario.rossi@gmail.com",
                LocalDate.now(),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32");
        newAccount.setName("Mario");
        newAccount.setSurname("Rossi");

        newAccountJpa = new AccountJpa(newAccount.getEmail(),
                newAccount.getName(),
                newAccount.getSurname(),
                newAccount.getBirthdate(),
                AccountJpa.Gender.valueOf(newAccount.getGender().toString()),
                newAccount.getPassword());

        savedAccountJpa = new AccountJpa(newAccountJpa.getEmail(),
                newAccountJpa.getName(),
                newAccountJpa.getSurname(),
                newAccountJpa.getBirthdate(),
                newAccountJpa.getGender(),
                newAccountJpa.getPassword());
        savedAccountJpa.setId(1L);

        convertedSavedAccount = new Account(savedAccountJpa.getEmail(),
                savedAccountJpa.getBirthdate(),
                Account.GenderEnum.valueOf(savedAccountJpa.getGender().toString()),
                savedAccountJpa.getPassword());
        convertedSavedAccount.setId(savedAccountJpa.getId());
        convertedSavedAccount.setName(savedAccountJpa.getName());
        convertedSavedAccount.setSurname(savedAccountJpa.getSurname());
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testSaveAccountSuccess() {
        when(this.accountsRepository.findByEmail(newAccount.getEmail())).thenReturn(Optional.empty());
        when(this.accountsToJpaConverter.convert(newAccount)).thenReturn(newAccountJpa);
        when(this.accountsRepository.save(newAccountJpa)).thenReturn(savedAccountJpa);
        when(this.accountsToJpaConverter.convertBack(savedAccountJpa)).thenReturn(convertedSavedAccount);

        Optional<Account> savedAccount = this.accountsService.save(newAccount);


        assertEquals(savedAccount.get().getId(), 1L);
        assertEquals(savedAccount.get().getEmail(), newAccount.getEmail());
        assertEquals(savedAccount.get().getName(), newAccount.getName());
        assertEquals(savedAccount.get().getSurname(), newAccount.getSurname());
        assertEquals(savedAccount.get().getBirthdate(), newAccount.getBirthdate());
        assertEquals(savedAccount.get().getGender(), newAccount.getGender());
        assertEquals(savedAccount.get().getPassword(), newAccount.getPassword());
        verify(this.accountsRepository, times(1)).findByEmail(newAccount.getEmail());
        verify(this.accountsRepository, times(1)).save(newAccountJpa);
    }

    @Test
    void testSaveAccountFailed(){
        when(this.accountsRepository.findByEmail(newAccount.getEmail())).thenReturn(Optional.ofNullable(newAccountJpa));

        assertThrows(AccountAlreadyCreated.class, () ->
            this.accountsService.save(newAccount)
        );

        verify(this.accountsRepository, times(1)).findByEmail(newAccount.getEmail());
        verify(this.accountsRepository, times(0)).save(Mockito.any(AccountJpa.class));

    }

    @Test
    void testDeleteAccountSuccess(){
        when(this.accountsRepository.findById(newAccountJpa.getId())).thenReturn(Optional.ofNullable(newAccountJpa));
        doNothing().when(this.accountsRepository).deleteById(newAccountJpa.getId());
        this.accountsService.remove(newAccountJpa.getId());
        verify(this.accountsRepository, times(1)).deleteById(newAccountJpa.getId());
    }

    @Test
    void testDeleteAccountFailed(){
        when(this.accountsRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(AccountIdNotFound.class,
                () -> this.accountsService.remove(1L)
        );

        verify(this.accountsRepository, times(0)).deleteById(newAccountJpa.getId());
    }
}