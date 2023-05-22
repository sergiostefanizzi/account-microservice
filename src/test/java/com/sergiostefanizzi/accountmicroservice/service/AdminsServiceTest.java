package com.sergiostefanizzi.accountmicroservice.service;

import com.sergiostefanizzi.accountmicroservice.controller.converter.AccountsToJpaConverter;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.repository.AccountsRepository;
import com.sergiostefanizzi.accountmicroservice.repository.AdminsRepository;
import com.sergiostefanizzi.accountmicroservice.repository.model.AccountJpa;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class AdminsServiceTest {
    @Mock
    private AdminsRepository adminsRepository;
    @Mock
    private AccountsRepository accountsRepository;
    @Mock
    private AccountsToJpaConverter accountsToJpaConverter;
    @InjectMocks
    private AdminsService adminsService;
    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testFindAllActive_Success() {
        List<AccountJpa> accountJpaList = new ArrayList<>();
        AccountJpa accountJpa1 = new AccountJpa(
          "acc1@gmail.com",
                "Mario",
                "Bros",
                LocalDate.of(1990,9,9),
                AccountJpa.Gender.MALE,
                "dsafdf!34fdsc"
        );
        accountJpa1.setId(1L);
        AccountJpa accountJpa2 = new AccountJpa(
          "acc2@gmail.com",
                "Luigi",
                "Bros",
                LocalDate.of(1995,2,7),
                AccountJpa.Gender.MALE,
                "deqw62_jed3eui"
        );
        accountJpa2.setId(2L);
        accountJpa2.setDeletedAt(Timestamp.valueOf(LocalDateTime.of(2022,4,5,12,15,1)));
        AccountJpa accountJpa3 = new AccountJpa(
          "acc3@gmail.com",
                "Checco",
                "Zalone",
                LocalDate.of(1995,2,7),
                AccountJpa.Gender.MALE,
                "deqw62_jed3eui"
        );
        accountJpa3.setId(3L);
        accountJpaList.add(accountJpa1);
        accountJpaList.add(accountJpa2);
        accountJpaList.add(accountJpa3);
        boolean removedAccount = false;

        when(this.accountsRepository.findAll()).thenReturn(accountJpaList);

        List<Account> accountList = this.adminsService.findAll(removedAccount);

        log.info("AccountJPAList size -> "+accountJpaList.size());
        log.info("AccountList size -> "+accountList.size());
        assertEquals(2,accountList.size());
        verify(this.accountsRepository, times(1)).findAll();

    }
    @Test
    void testFindAllRemoved_Success() {
        List<AccountJpa> accountJpaList = new ArrayList<>();
        AccountJpa accountJpa1 = new AccountJpa(
                "acc1@gmail.com",
                "Mario",
                "Bros",
                LocalDate.of(1990,9,9),
                AccountJpa.Gender.MALE,
                "dsafdf!34fdsc"
        );
        accountJpa1.setId(1L);
        AccountJpa accountJpa2 = new AccountJpa(
                "acc2@gmail.com",
                "Luigi",
                "Bros",
                LocalDate.of(1995,2,7),
                AccountJpa.Gender.MALE,
                "deqw62_jed3eui"
        );
        accountJpa2.setId(2L);
        accountJpa2.setDeletedAt(Timestamp.valueOf(LocalDateTime.of(2022,4,5,12,15,1)));
        AccountJpa accountJpa3 = new AccountJpa(
                "acc3@gmail.com",
                "Checco",
                "Zalone",
                LocalDate.of(1995,2,7),
                AccountJpa.Gender.MALE,
                "deqw62_jed3eui"
        );
        accountJpa3.setId(3L);
        accountJpaList.add(accountJpa1);
        accountJpaList.add(accountJpa2);
        accountJpaList.add(accountJpa3);
        boolean removedAccount = true;
        when(this.accountsRepository.findAll()).thenReturn(accountJpaList);

        List<Account> accountList = this.adminsService.findAll(removedAccount);
        log.info("AccountJPAList size -> "+accountJpaList.size());
        log.info("AccountList size -> "+accountList.size());
        assertEquals(1,accountList.size());
        verify(this.accountsRepository, times(1)).findAll();

    }
}