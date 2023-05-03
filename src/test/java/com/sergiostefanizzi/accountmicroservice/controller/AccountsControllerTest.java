package com.sergiostefanizzi.accountmicroservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AccountAlreadyCreatedException;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AccountIdNotFoundException;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AccountNotFoundException;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.UpdateAccountByIdRequest;
import com.sergiostefanizzi.accountmicroservice.service.AccountsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    UpdateAccountByIdRequest accountToUpdate;
    Long accountId = 1L;

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
        savedAccount.setId(accountId);

        accountToUpdate = new UpdateAccountByIdRequest();

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testAddAccountSuccess() throws Exception {

        String json = this.objectMapper.writeValueAsString(newAccount);
        when(this.accountsService.save(newAccount)).thenReturn(savedAccount);

        this.mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON).content(json)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(savedAccount.getId()))
                .andExpect(jsonPath("$.email").value(savedAccount.getEmail()))
                .andExpect(jsonPath("$.name").value(savedAccount.getName()))
                .andExpect(jsonPath("$.surname").value(savedAccount.getSurname()))
                .andExpect(jsonPath("$.birthdate").value(savedAccount.getBirthdate().toString()))
                .andExpect(jsonPath("$.gender").value(savedAccount.getGender().toString()))
                .andExpect(jsonPath("$.password").value(savedAccount.getPassword()));;
    }

    @Test
    void testAddAccountFailed() throws Exception{
        String newAccountJson = this.objectMapper.writeValueAsString(newAccount);
        when(this.accountsService.save(newAccount)).thenThrow(AccountAlreadyCreatedException.class);

        this.mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newAccountJson)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void testAddAccountFailed_MissingEmail() throws Exception {
        newAccount.setEmail(null);
        //newAccount.setGender(null);
        String newAccountJson = this.objectMapper.writeValueAsString(newAccount);

        this.mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccountJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("email is mandatory"));
                //.andExpect(jsonPath("$.errors[1]").value("gender is mandatory"));
    }

    @Test
    void testDeleteAccountSuccess() throws Exception{
        doNothing().when(this.accountsService).remove(accountId);

        this.mockMvc.perform(delete("/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

    }

    @Test
    void testDeleteAccountFailed() throws Exception{
        doThrow(AccountIdNotFoundException.class).when(this.accountsService).remove(accountId);
        this.mockMvc.perform(delete("/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad request! Id is not valid"));

    }

    @Test
    void testUpdateAccountSuccess() throws Exception{
        //account con campi aggiornati
        accountToUpdate.setName(newAccount.getName());
        accountToUpdate.setSurname(newAccount.getSurname());
        accountToUpdate.setGender(UpdateAccountByIdRequest.GenderEnum.valueOf(newAccount.getGender().toString()));
        accountToUpdate.setPassword("hhh3h2h234h4");

        //account che mi aspetto di ricevere
        Account updatedAccount = new Account(
                newAccount.getEmail(),
                newAccount.getBirthdate(),
                newAccount.getGender(),
                accountToUpdate.getPassword()
        );
        updatedAccount.setId(accountId);
        updatedAccount.setName(accountToUpdate.getName());
        updatedAccount.setSurname(accountToUpdate.getSurname());

        //converto l'account che voglio aggiornare in formato json
        String accountToUpdateJson = this.objectMapper.writeValueAsString(accountToUpdate);

        when(this.accountsService.update(accountId, accountToUpdate)).thenReturn(updatedAccount);

        this.mockMvc.perform(patch("/accounts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(accountToUpdateJson)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedAccount.getId()))
                .andExpect(jsonPath("$.email").value(updatedAccount.getEmail()))
                .andExpect(jsonPath("$.name").value(updatedAccount.getName()))
                .andExpect(jsonPath("$.surname").value(updatedAccount.getSurname()))
                .andExpect(jsonPath("$.birthdate").value(updatedAccount.getBirthdate().toString()))
                .andExpect(jsonPath("$.gender").value(updatedAccount.getGender().toString()))
                .andExpect(jsonPath("$.password").value(updatedAccount.getPassword()));

    }

    @Test
    void testUpdateAccountFailed() throws Exception{
        //account con campi aggiornati
        accountToUpdate.setName(newAccount.getName());
        accountToUpdate.setSurname(newAccount.getSurname());
        accountToUpdate.setGender(UpdateAccountByIdRequest.GenderEnum.valueOf(newAccount.getGender().toString()));
        accountToUpdate.setPassword("hhh3h2h234h4");


        //converto l'account che voglio aggiornare in formato json
        String accountToUpdateJson = this.objectMapper.writeValueAsString(accountToUpdate);

        when(this.accountsService.update(accountId, accountToUpdate)).thenThrow(AccountNotFoundException.class);

        this.mockMvc.perform(patch("/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(accountToUpdateJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Account not found!"));

    }

}