package com.sergiostefanizzi.accountmicroservice.service;

import com.sergiostefanizzi.accountmicroservice.controller.converter.AccountToJpaConverter;
import com.sergiostefanizzi.accountmicroservice.controller.converter.AccountJpaToAdminConverter;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AccountNotFoundException;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AdminAlreadyCreatedException;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.Admin;
import com.sergiostefanizzi.accountmicroservice.repository.AccountsRepository;
import com.sergiostefanizzi.accountmicroservice.model.AccountJpa;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class AdminsServiceTest {
    @Mock
    private AccountsRepository accountsRepository;
    @Mock
    private AccountToJpaConverter accountToJpaConverter;
    @Mock
    private AccountJpaToAdminConverter accountJpaToAdminConverter;
    @InjectMocks
    private AdminsService adminsService;
    Long accountId = 1L;
    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    //FIND ALL
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
        verify(this.accountToJpaConverter, times(accountList.size())).convertBack(any(AccountJpa.class));

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
        verify(this.accountToJpaConverter, times(accountList.size())).convertBack(any(AccountJpa.class));
    }

    //SAVE
    @Test
    void testSave_Success(){

        AccountJpa adminToBe = new AccountJpa(
                "mario.rossi@gmail.com",
                "Mario",
                "Rossi",
                LocalDate.of(1994,4,6),
                AccountJpa.Gender.MALE,
                "fsfweewr423!gff"
        );
        adminToBe.setId(accountId);


        Admin convertedAdmin = new Admin(accountId);

        when(this.accountsRepository.findById(accountId)).thenReturn(Optional.of(adminToBe));
        when(this.accountsRepository.save(adminToBe)).thenReturn(adminToBe);
        when(this.accountJpaToAdminConverter.convert(adminToBe)).thenReturn(convertedAdmin);

        Admin savedAdmin = this.adminsService.save(accountId);
        assertEquals(savedAdmin.getId(),accountId);
        assertTrue(adminToBe.getAdmin());
        verify(this.accountsRepository,times(1)).findById(accountId);
        verify(this.accountsRepository, times(1)).save(adminToBe);
        verify(this.accountJpaToAdminConverter, times(1)).convert(adminToBe);
    }

    @Test
    void testSave_NotFound_Failed(){
        when(this.accountsRepository.findById(accountId)).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class,
                () -> this.adminsService.save(accountId)
        );
        verify(this.accountsRepository,times(1)).findById(accountId);
        verify(this.accountsRepository, times(0)).save(any(AccountJpa.class));
    }

    @Test
    void testSave_AlreadyCreated_Failed(){
        AccountJpa alreadyAdmin = new AccountJpa(
                "mario.rossi@gmail.com",
                "Mario",
                "Rossi",
                LocalDate.of(1994,4,6),
                AccountJpa.Gender.MALE,
                "fsfweewr423!gff"
        );
        alreadyAdmin.setId(accountId);
        alreadyAdmin.setAdmin(true);

        when(this.accountsRepository.findById(accountId)).thenReturn(Optional.of(alreadyAdmin));


        assertThrows(AdminAlreadyCreatedException.class,
                () -> this.adminsService.save(accountId)
        );
        verify(this.accountsRepository,times(1)).findById(accountId);
        verify(this.accountsRepository, times(0)).save(any(AccountJpa.class));
    }

    //REMOVE
    @Test
    void testRemove_ByAdmin_Success(){

        AccountJpa accountToDeleteJpa = new AccountJpa(
                "mario.rossi@gmail.com",
                "Mario",
                "Rossi",
                LocalDate.of(1994,4,6),
                AccountJpa.Gender.MALE,
                "fsfweewr423!gff"
        );
        accountToDeleteJpa.setId(accountId);


        when(this.accountsRepository.findById(accountId)).thenReturn(Optional.of(accountToDeleteJpa));
        when(this.accountsRepository.save(accountToDeleteJpa)).thenReturn(accountToDeleteJpa);

        this.adminsService.remove(accountId);

        assertNotNull(accountToDeleteJpa.getDeletedAt());
        verify(this.accountsRepository, times(1)).findById(accountId);
        verify(this.accountsRepository, times(1)).save(accountToDeleteJpa);

    }

    @Test
    void testRemove_NotFound_Failed(){
        when(this.accountsRepository.findById(accountId)).thenReturn(Optional.empty());


        assertThrows(AccountNotFoundException.class,
                () -> this.adminsService.remove(accountId)
        );

        verify(this.accountsRepository, times(1)).findById(accountId);
        verify(this.accountsRepository, times(0)).save(any(AccountJpa.class));

    }

    @Test
    void testRemove_AlreadyRemoved_Failed(){
        AccountJpa accountDeletedJpa = new AccountJpa(
                "mario.rossi@gmail.com",
                "Mario",
                "Rossi",
                LocalDate.of(1994,4,6),
                AccountJpa.Gender.MALE,
                "fsfweewr423!gff"
        );
        accountDeletedJpa.setId(accountId);
        accountDeletedJpa.setDeletedAt(Timestamp.valueOf(LocalDateTime.of(2023,5,6,4,6,7)));

        when(this.accountsRepository.findById(accountId)).thenReturn(Optional.of(accountDeletedJpa));


        assertThrows(AccountNotFoundException.class,
                () -> this.adminsService.remove(accountId)
        );

        verify(this.accountsRepository, times(1)).findById(accountId);
        verify(this.accountsRepository, times(0)).save(any(AccountJpa.class));

    }



}