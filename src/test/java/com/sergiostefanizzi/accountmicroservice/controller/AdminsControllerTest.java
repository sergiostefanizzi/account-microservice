package com.sergiostefanizzi.accountmicroservice.controller;

import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.service.AdminsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest(AdminsController.class)
class AdminsControllerTest {
    @MockBean
    private AdminsService adminsService;
    @Autowired
    private MockMvc mockMvc;
    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void addAdminById() {
    }

    @Test
    void deleteAdminById() {
    }

    @Test
    void testFindAllAccounts_Then_200() throws Exception{
        List<Account> accountList = new ArrayList<>();
        Account account1 = new Account(
          "acc1@gmail.com",
          LocalDate.of(1990,3,4),
                Account.GenderEnum.MALE,
                "Cdifdf23!!"
        );
        account1.setId(1L);
        Account account2 = new Account(
                "acc2@gmail.com",
                LocalDate.of(1995,4,14),
                Account.GenderEnum.FEMALE,
                "XXfdsdf23!4!"
        );
        account2.setId(2L);
        Account account3 = new Account(
                "acc3@gmail.com",
                LocalDate.of(2000,5,5),
                Account.GenderEnum.MALE,
                "Cdirtwqwqqqf2563_"
        );
        account3.setId(3L);
        accountList.add(account1);
        accountList.add(account2);
        accountList.add(account3);
        boolean removedAccount = false;
        when(this.adminsService.findAll(removedAccount)).thenReturn(accountList);

        this.mockMvc.perform(get("/admins/accounts")
                        .queryParam("removedAccount", String.valueOf(removedAccount))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(accountList.size())))
                .andExpect(jsonPath("$.[0].id").value(account1.getId()))
                .andExpect(jsonPath("$.[0].email").value(account1.getEmail()))
                .andExpect(jsonPath("$.[0].name").value(account1.getName()))
                .andExpect(jsonPath("$.[0].surname").value(account1.getSurname()))
                .andExpect(jsonPath("$.[0].birthdate").value(account1.getBirthdate().toString()))
                .andExpect(jsonPath("$.[0].gender").value(account1.getGender().toString()))
                .andExpect(jsonPath("$.[1].id").value(account2.getId()))
                .andExpect(jsonPath("$.[1].email").value(account2.getEmail()))
                .andExpect(jsonPath("$.[1].name").value(account2.getName()))
                .andExpect(jsonPath("$.[1].surname").value(account2.getSurname()))
                .andExpect(jsonPath("$.[1].birthdate").value(account2.getBirthdate().toString()))
                .andExpect(jsonPath("$.[1].gender").value(account2.getGender().toString()))
                .andExpect(jsonPath("$.[2].id").value(account3.getId()))
                .andExpect(jsonPath("$.[2].email").value(account3.getEmail()))
                .andExpect(jsonPath("$.[2].name").value(account3.getName()))
                .andExpect(jsonPath("$.[2].surname").value(account3.getSurname()))
                .andExpect(jsonPath("$.[2].birthdate").value(account3.getBirthdate().toString()))
                .andExpect(jsonPath("$.[2].gender").value(account3.getGender().toString()));
    }

    @Test
    void testFindAllAccounts_Then_400() throws Exception{
        List<Account> accountList = new ArrayList<>();
        Account account1 = new Account(
                "acc1@gmail.com",
                LocalDate.of(1990,3,4),
                Account.GenderEnum.MALE,
                "Cdifdf23!!"
        );
        account1.setId(1L);
        Account account2 = new Account(
                "acc2@gmail.com",
                LocalDate.of(1995,4,14),
                Account.GenderEnum.FEMALE,
                "XXfdsdf23!4!"
        );
        account2.setId(2L);
        Account account3 = new Account(
                "acc3@gmail.com",
                LocalDate.of(2000,5,5),
                Account.GenderEnum.MALE,
                "Cdirtwqwqqqf2563_"
        );
        account3.setId(3L);
        accountList.add(account1);
        accountList.add(account2);
        accountList.add(account3);

        when(this.adminsService.findAll(any(Boolean.class))).thenReturn(accountList);

        this.mockMvc.perform(get("/admins/accounts")
                        .queryParam("removedAccount", "notbool")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isNotEmpty());

    }
}