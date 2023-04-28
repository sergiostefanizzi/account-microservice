package com.sergiostefanizzi.accountmicroservice.service;

import com.sergiostefanizzi.accountmicroservice.controller.converter.AccountsToJpaConverter;
import com.sergiostefanizzi.accountmicroservice.exceptions.AccountAlreadyCreatedException;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.repository.AccountsRepository;
import com.sergiostefanizzi.accountmicroservice.repository.model.AccountJpa;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

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
    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testSaveSuccess() {
        Account newAccount = new Account("mario.rossi@gmail.com",
                LocalDate.now(),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32");
        newAccount.setName("Mario");
        newAccount.setSurname("Rossi");

        AccountJpa newAccountJpa = new AccountJpa(newAccount.getEmail(),
                newAccount.getName(),
                newAccount.getSurname(),
                newAccount.getBirthdate(),
                AccountJpa.Gender.valueOf(newAccount.getGender().toString()),
                newAccount.getPassword());

        AccountJpa savedAccountJpa = new AccountJpa(newAccountJpa.getEmail(),
                newAccountJpa.getName(),
                newAccountJpa.getSurname(),
                newAccountJpa.getBirthdate(),
                newAccountJpa.getGender(),
                newAccountJpa.getPassword());
        savedAccountJpa.setId(1L);

        Account convertedSavedAccount = new Account(savedAccountJpa.getEmail(),
                savedAccountJpa.getBirthdate(),
                Account.GenderEnum.valueOf(savedAccountJpa.getGender().toString()),
                savedAccountJpa.getPassword());
        convertedSavedAccount.setId(savedAccountJpa.getId());
        convertedSavedAccount.setName(savedAccountJpa.getName());
        convertedSavedAccount.setSurname(savedAccountJpa.getSurname());

        when(this.accountsToJpaConverter.convert(newAccount)).thenReturn(newAccountJpa);
        when(this.accountsRepository.findbyEmail(newAccount.getEmail())).thenReturn(Optional.empty());
        when(this.accountsRepository.save(newAccountJpa)).thenReturn(savedAccountJpa);
        when(this.accountsToJpaConverter.convertBack(savedAccountJpa)).thenReturn(convertedSavedAccount);

        Optional<Account> savedAccount= accountsService.save(newAccount);

        assertTrue(this.accountsRepository.findbyEmail(newAccount.getEmail()).isEmpty());
        assertEquals(savedAccount.get().getId(), 1L);
        assertEquals(savedAccount.get().getEmail(), newAccount.getEmail());
        assertEquals(savedAccount.get().getName(), newAccount.getName());
        assertEquals(savedAccount.get().getSurname(), newAccount.getSurname());
        assertEquals(savedAccount.get().getBirthdate(), newAccount.getBirthdate());
        assertEquals(savedAccount.get().getGender(), newAccount.getGender());
        assertEquals(savedAccount.get().getPassword(), newAccount.getPassword());
        verify(this.accountsRepository, times(1)).save(newAccountJpa);


    }


}