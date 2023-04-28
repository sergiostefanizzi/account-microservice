package com.sergiostefanizzi.accountmicroservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.service.AccountsService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest(AccountsController.class)
class AccountsControllerTest {

    @MockBean
    private AccountsService accountsService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void addAccount() throws Exception{
        Account newAccount = new Account("mario.rossi@gmail.com",
                LocalDate.now(),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32");
        newAccount.setName("Mario");
        newAccount.setSurname("Rossi");

        Account savedAccount = new Account("mario.rossi@gmail.com",
                LocalDate.now(),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32");
        newAccount.setName("Mario");
        newAccount.setSurname("Rossi");
         savedAccount.setId(1L);
        String json = this.objectMapper.writeValueAsString(newAccount);
        when(this.accountsService.save(newAccount)).thenReturn(Optional.of(savedAccount));

        this.mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON).content(json)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(savedAccount.getName()));

    }
}