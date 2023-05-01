package com.sergiostefanizzi.accountmicroservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AccountAlreadyCreated;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AccountIdNotFound;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

    Account newAccount;
    Account savedAccount;

    @BeforeEach
    void setUp() {
        newAccount = new Account("mario.rossi@gmail.com",
                LocalDate.now(),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32");
        newAccount.setName("Mario");
        newAccount.setSurname("Rossi");

        savedAccount = new Account("mario.rossi@gmail.com",
                LocalDate.now(),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32");
        newAccount.setName("Mario");
        newAccount.setSurname("Rossi");
        savedAccount.setId(1L);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testAddAccountSuccess() throws Exception {

        String json = this.objectMapper.writeValueAsString(newAccount);
        when(this.accountsService.save(newAccount)).thenReturn(Optional.ofNullable(savedAccount));

        this.mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON).content(json)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(savedAccount.getId()))
                .andExpect(jsonPath("$.email").value(savedAccount.getEmail()))
                .andExpect(jsonPath("$.name").value(savedAccount.getName()))
                .andExpect(jsonPath("$.surname").value(savedAccount.getSurname()))
                .andExpect(jsonPath("$.birthdate").value(savedAccount.getBirthdate().toString()))
                .andExpect(jsonPath("$.gender").value(savedAccount.getGender().toString()));
    }

    @Test
    void testAddAccountFailed() throws Exception{
        String json = this.objectMapper.writeValueAsString(newAccount);
        when(this.accountsService.save(newAccount)).thenThrow(AccountAlreadyCreated.class);

        this.mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void testAddAccountFailed_MissingEmailAndGender() throws Exception {
        newAccount.setEmail(null);
        newAccount.setGender(null);
        String json = this.objectMapper.writeValueAsString(newAccount);

        this.mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("email is mandatory"))
                .andExpect(jsonPath("$.errors[1]").value("gender is mandatory"));
    }

    @Test
    void testDeleteAccountSuccess() throws Exception{
        doNothing().when(this.accountsService).remove(1L);

        this.mockMvc.perform(delete("/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

    }

    @Test
    void testDeleteAccountFailed() throws Exception{
        doThrow(AccountIdNotFound.class).when(this.accountsService).remove(1L);
        this.mockMvc.perform(delete("/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad request! Id is not valid"));

    }

}