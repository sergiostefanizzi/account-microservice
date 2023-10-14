package com.sergiostefanizzi.accountmicroservice.service;

import com.sergiostefanizzi.accountmicroservice.controller.converter.AccountToJpaConverter;
import com.sergiostefanizzi.accountmicroservice.controller.converter.AccountJpaToAdminConverter;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.Admin;
import com.sergiostefanizzi.accountmicroservice.repository.AccountsRepository;
import com.sergiostefanizzi.accountmicroservice.model.AccountJpa;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AccountAlreadyCreatedException;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AdminAlreadyCreatedException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
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
    private Account savedAccount1;
    private Account savedAccount2;
    private Account savedAccount3;
    private AccountJpa savedAccountJpa1;
    private AccountJpa savedAccountJpa2;
    private AccountJpa savedAccountJpa3;
    private final LocalDateTime validationTime = LocalDateTime.now();
    private final List<String> validationCodeList = asList(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString()
    );

    Long accountId = 1L;
    @BeforeEach
    void setUp() {
        this.savedAccount1 = new Account("pinco.pallino@gmail.com",
                LocalDate.of(1970,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        this.savedAccount1.setName("Pinco");
        this.savedAccount1.setSurname("Pallino");
        this.savedAccount1.setId(101L);

        this.savedAccountJpa1 = new AccountJpa(
                this.savedAccount1.getEmail(),
                this.savedAccount1.getBirthdate(),
                this.savedAccount1.getGender(),
                this.savedAccount1.getPassword()
        );
        this.savedAccountJpa1.setName(this.savedAccount1.getName());
        this.savedAccountJpa1.setSurname(this.savedAccount1.getSurname());
        this.savedAccountJpa1.setValidationCode(validationCodeList.get(0));
        this.savedAccountJpa1.setValidatedAt(this.validationTime);
        this.savedAccountJpa1.setId(this.savedAccount1.getId());
        this.savedAccountJpa1.setIsAdmin(false);

        this.savedAccount2 = new Account("mario_bros@live.it",
                LocalDate.of(1995,2,1),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        this.savedAccount2.setName("Mario");
        this.savedAccount2.setSurname("Bros");
        this.savedAccount2.setId(102L);

        this.savedAccountJpa2 = new AccountJpa(
                this.savedAccount2.getEmail(),
                this.savedAccount2.getBirthdate(),
                this.savedAccount2.getGender(),
                this.savedAccount2.getPassword()
        );
        this.savedAccountJpa2.setName(this.savedAccount2.getName());
        this.savedAccountJpa2.setSurname(this.savedAccount2.getSurname());
        this.savedAccountJpa2.setValidationCode(validationCodeList.get(1));
        this.savedAccountJpa2.setValidatedAt(this.validationTime);
        this.savedAccountJpa2.setId(this.savedAccount2.getId());
        this.savedAccountJpa2.setIsAdmin(false);

        this.savedAccount3 = new Account("luigi_bro@outlook.com",
                LocalDate.of(1995,2,1),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        this.savedAccount3.setName("Luigi");
        this.savedAccount3.setSurname("Bros");
        this.savedAccount3.setId(103L);

        this.savedAccountJpa3 = new AccountJpa(
                this.savedAccount3.getEmail(),
                this.savedAccount3.getBirthdate(),
                this.savedAccount3.getGender(),
                this.savedAccount3.getPassword()
        );
        this.savedAccountJpa3.setName(this.savedAccount3.getName());
        this.savedAccountJpa3.setSurname(this.savedAccount3.getSurname());
        this.savedAccountJpa3.setValidationCode(validationCodeList.get(2));
        this.savedAccountJpa3.setValidatedAt(this.validationTime);
        this.savedAccountJpa3.setId(this.savedAccount3.getId());
        this.savedAccountJpa3.setIsAdmin(false);
    }

    @AfterEach
    void tearDown() {
    }

    //FIND ALL
    @Test
    void testFindAllActive_Success() {
        List<AccountJpa> activeAccountJpaList = asList(
                this.savedAccountJpa1,
                this.savedAccountJpa2,
                this.savedAccountJpa3
        );

        List<Account> activeAccountList = asList(
                this.savedAccount1,
                this.savedAccount2,
                this.savedAccount3
        );
        when(this.accountsRepository.findAllActive()).thenReturn(activeAccountJpaList);
        when(this.accountToJpaConverter.convertBack(activeAccountJpaList.get(0))).thenReturn(activeAccountList.get(0));
        when(this.accountToJpaConverter.convertBack(activeAccountJpaList.get(1))).thenReturn(activeAccountList.get(1));
        when(this.accountToJpaConverter.convertBack(activeAccountJpaList.get(2))).thenReturn(activeAccountList.get(2));

        List<Account> returnedList = this.adminsService.findAll(false);

        assertEquals(activeAccountList, returnedList);
        verify(this.accountsRepository, times(1)).findAllActive();
        verify(this.accountsRepository, times(0)).findAllActiveAndDeleted();
        verify(this.accountToJpaConverter, times(3)).convertBack(any(AccountJpa.class));
        log.info(returnedList.toString());

    }

    @Test
    void testFindAllRemoved_Success() {
        this.savedAccountJpa2.setValidatedAt(null);
        List<AccountJpa> activeAndRemovedAccountJpaList = asList(
                this.savedAccountJpa1,
                this.savedAccountJpa2,
                this.savedAccountJpa3
        );

        List<Account> activeAndRemovedAccountList = asList(
                this.savedAccount1,
                this.savedAccount2,
                this.savedAccount3
        );
        when(this.accountsRepository.findAllActiveAndDeleted()).thenReturn(activeAndRemovedAccountJpaList);
        when(this.accountToJpaConverter.convertBack(activeAndRemovedAccountJpaList.get(0))).thenReturn(activeAndRemovedAccountList.get(0));
        when(this.accountToJpaConverter.convertBack(activeAndRemovedAccountJpaList.get(1))).thenReturn(activeAndRemovedAccountList.get(1));
        when(this.accountToJpaConverter.convertBack(activeAndRemovedAccountJpaList.get(2))).thenReturn(activeAndRemovedAccountList.get(2));

        List<Account> returnedList = this.adminsService.findAll(true);

        assertEquals(activeAndRemovedAccountList, returnedList);
        verify(this.accountsRepository, times(1)).findAllActiveAndDeleted();
        verify(this.accountsRepository, times(0)).findAllActive();
        verify(this.accountToJpaConverter, times(3)).convertBack(any(AccountJpa.class));

        log.info(returnedList.toString());
    }

    //SAVE
    @Test
    void testSave_Success(){
        Admin newAdmin = new Admin(this.savedAccountJpa1.getId());
        when(this.accountsRepository.getReferenceById(anyLong())).thenReturn(this.savedAccountJpa1);
        when(this.accountsRepository.save(any(AccountJpa.class))).thenReturn(this.savedAccountJpa1);
        when(this.accountJpaToAdminConverter.convert(any(AccountJpa.class))).thenReturn(newAdmin);

        Admin returnedAdmin = this.adminsService.save(this.savedAccountJpa1.getId());

        assertEquals(newAdmin, returnedAdmin);
        verify(this.accountsRepository, times(1)).getReferenceById(anyLong());
        verify(this.accountsRepository, times(1)).save(any(AccountJpa.class));
        verify(this.accountJpaToAdminConverter, times(1)).convert(any(AccountJpa.class));

        log.info(returnedAdmin.toString());
    }


    @Test
    void testSave_AlreadyCreated_Failed(){
        this.savedAccountJpa1.setIsAdmin(true);

        when(this.accountsRepository.getReferenceById(anyLong())).thenReturn(this.savedAccountJpa1);

        assertThrows(AdminAlreadyCreatedException.class, () ->
                this.adminsService.save(this.savedAccountJpa1.getId())
        );

        verify(this.accountsRepository, times(1)).getReferenceById(anyLong());
        verify(this.accountsRepository, times(0)).save(any(AccountJpa.class));
        verify(this.accountJpaToAdminConverter, times(0)).convert(any(AccountJpa.class));

    }

    //REMOVE
    @Test
    void testRemove_ByAdmin_Success(){
        when(this.accountsRepository.getReferenceById(anyLong())).thenReturn(this.savedAccountJpa1);
        when(this.accountsRepository.save(any(AccountJpa.class))).thenReturn(this.savedAccountJpa1);

        this.adminsService.remove(this.savedAccountJpa1.getId());

        assertNotNull(this.savedAccountJpa1.getDeletedAt());
        verify(this.accountsRepository, times(1)).getReferenceById(anyLong());
        verify(this.accountsRepository, times(1)).save(any(AccountJpa.class));

        log.info(this.savedAccountJpa1.getDeletedAt().toString());
    }




}