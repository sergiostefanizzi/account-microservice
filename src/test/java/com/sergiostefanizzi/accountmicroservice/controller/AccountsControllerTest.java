package com.sergiostefanizzi.accountmicroservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AccountAlreadyCreatedException;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AccountNotFoundException;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.AccountPatch;
import com.sergiostefanizzi.accountmicroservice.service.AccountsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    Long accountId = 1L;

    @BeforeEach
    void setUp() {


    }

    @AfterEach
    void tearDown() {
    }

    // Add account Success
    @Test
    void testAddFullAccountSuccess() throws Exception {

        Account newAccount = new Account("mario.rossi@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        newAccount.setName("Mario");
        newAccount.setSurname("Rossi");

        Account savedAccount = new Account("mario.rossi@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32");
        newAccount.setName("Mario");
        newAccount.setSurname("Rossi");
        savedAccount.setId(accountId);


        String newAccountJson = this.objectMapper.writeValueAsString(newAccount);
        when(this.accountsService.save(newAccount)).thenReturn(savedAccount);

        this.mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON).content(newAccountJson)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(savedAccount.getId()))
                .andExpect(jsonPath("$.email").value(savedAccount.getEmail()))
                .andExpect(jsonPath("$.name").value(savedAccount.getName()))
                .andExpect(jsonPath("$.surname").value(savedAccount.getSurname()))
                .andExpect(jsonPath("$.birthdate").value(savedAccount.getBirthdate().toString()))
                .andExpect(jsonPath("$.gender").value(savedAccount.getGender().toString()))
                .andExpect(jsonPath("$.password").value(savedAccount.getPassword()));
    }

    @Test
    void testAddAccountSuccess_Missing_Name_Surname() throws Exception {
        Account newAccount = new Account("mario.rossi@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        //newAccount.setSurname("Rossi");

        Account savedAccount = new Account("mario.rossi@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32");
        //newAccount.setSurname("Rossi");
        savedAccount.setId(accountId);

        String newAccountJson = this.objectMapper.writeValueAsString(newAccount);
        when(this.accountsService.save(newAccount)).thenReturn(savedAccount);

        this.mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccountJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(savedAccount.getId()))
                .andExpect(jsonPath("$.email").value(savedAccount.getEmail()))
                .andExpect(jsonPath("$.name").value(savedAccount.getName()))
                .andExpect(jsonPath("$.surname").value(savedAccount.getSurname()))
                .andExpect(jsonPath("$.birthdate").value(savedAccount.getBirthdate().toString()))
                .andExpect(jsonPath("$.gender").value(savedAccount.getGender().toString()))
                .andExpect(jsonPath("$.password").value(savedAccount.getPassword()));
    }

    // Add Account Failed

    @Test
    void testAddAccountFailedAlreadyCreated() throws Exception{
        Account newAccount = new Account("mario.rossi@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        newAccount.setName("Mario");
        newAccount.setSurname("Rossi");

        String newAccountJson = this.objectMapper.writeValueAsString(newAccount);
        when(this.accountsService.save(newAccount)).thenThrow(new AccountAlreadyCreatedException(newAccount.getEmail()));


        this.mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newAccountJson)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict! Account with email "+newAccount.getEmail()+" already created!"));
    }

    @Test
    void testAddAccountFailed_MissingRequiredFields() throws Exception {
        Account newAccount = new Account(null,
                null,
                null,
                null);
        newAccount.setName("Mario");
        newAccount.setSurname("Rossi");
        String newAccountJson = this.objectMapper.writeValueAsString(newAccount);

        List<String> errors = new ArrayList<>();
        errors.add("email must not be null");
        errors.add("birthdate must not be null");
        errors.add("gender must not be null");
        errors.add("password must not be null");

        this.mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccountJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error[0]").value(in(errors)))
                .andExpect(jsonPath("$.error[1]").value(in(errors)))
                .andExpect(jsonPath("$.error[2]").value(in(errors)))
                .andExpect(jsonPath("$.error[3]").value(in(errors)));
    }

    @Test
    void testAddAccountFailed_InvalidFields() throws Exception {
        Account newAccount = new Account("mario.rossigmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32");
        newAccount.setName("Mario3");
        newAccount.setSurname("Rossi3");
        String newAccountJson = this.objectMapper.writeValueAsString(newAccount);

        List<String> errors = new ArrayList<>();
        errors.add("email must be a well-formed email address");
        errors.add("password must match \"^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$\"");
        errors.add("name must match \"^[a-zA-Z]+$\"");
        errors.add("surname must match \"^[a-zA-Z]+$\"");


        this.mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccountJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error[0]").value(in(errors)))
                .andExpect(jsonPath("$.error[1]").value(in(errors)))
                .andExpect(jsonPath("$.error[2]").value(in(errors)))
                .andExpect(jsonPath("$.error[3]").value(in(errors)));
    }

    @Test
    void testAddAccountFailed_FieldSize() throws Exception {
        Account newAccount = new Account("m@",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "ds3!");
        newAccount.setName("M");
        newAccount.setSurname("R");
        String newAccountJson = this.objectMapper.writeValueAsString(newAccount);
        List<String> errors = new ArrayList<>();
        errors.add("email must be a well-formed email address");
        errors.add("email size must be between 3 and 320");
        errors.add("password must match \"^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$\"");
        errors.add("password size must be between 8 and 255");
        errors.add("name size must be between 2 and 50");
        errors.add("surname size must be between 2 and 50");
        this.mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccountJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error[0]").value(in(errors)))
                .andExpect(jsonPath("$.error[1]").value(in(errors)))
                .andExpect(jsonPath("$.error[2]").value(in(errors)))
                .andExpect(jsonPath("$.error[3]").value(in(errors)))
                .andExpect(jsonPath("$.error[4]").value(in(errors)))
                .andExpect(jsonPath("$.error[5]").value(in(errors)));
    }



    // Add Account Failed BIRTHDATE

    @Test
    void testAddAccountFailed_Birthdate_Type() throws Exception {
        Account newAccount = new Account("mario.rossi@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        newAccount.setName("Mario");
        newAccount.setSurname("Rossi");
        //String newAccountJson = this.objectMapper.writeValueAsString(newAccount);
        JsonNode jsonNode = this.objectMapper.readTree(this.objectMapper.writeValueAsString(newAccount));
        ((ObjectNode) jsonNode).put("birthdate","202308-05");
        String newAccountJson = this.objectMapper.writeValueAsString(jsonNode);
        this.mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccountJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("JSON parse error: Cannot deserialize value of type `java.time.LocalDate` from String \"202308-05\": Failed to deserialize java.time.LocalDate: (java.time.format.DateTimeParseException) Text '202308-05' could not be parsed at index 0"));
    }

    @Test
    void testAddAccountFailed_Birthdate_UnderAge() throws Exception {
        Account newAccount = new Account("mario.rossi@gmail.com",
                LocalDate.now(),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        newAccount.setName("Mario");
        newAccount.setSurname("Rossi");

        String newAccountJson = this.objectMapper.writeValueAsString(newAccount);

        this.mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccountJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("birthdate is not valid! The user must be an adult"));
    }

    @Test
    void testAddAccountFailed_Birthdate_NotPastOrPresent() throws Exception {
        Account newAccount = new Account("mario.rossi@gmail.com",
                LocalDate.of(2100,2,2),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        newAccount.setName("Mario");
        newAccount.setSurname("Rossi");

        String newAccountJson = this.objectMapper.writeValueAsString(newAccount);

        this.mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccountJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("birthdate must be a date in the past or in the present"));
    }

    // Add Account Failed GENDER

    @Test
    void testAddAccountFailed_Gender_Type() throws Exception {
        Account newAccount = new Account("mario.rossi@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        newAccount.setName("Mario");
        newAccount.setSurname("Rossi");
        //String newAccountJson = this.objectMapper.writeValueAsString(newAccount);
        JsonNode jsonNode = this.objectMapper.readTree(this.objectMapper.writeValueAsString(newAccount));
        ((ObjectNode) jsonNode).put("gender","male");
        String newAccountJson = this.objectMapper.writeValueAsString(jsonNode);
        this.mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccountJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("JSON parse error: Cannot construct instance of `com.sergiostefanizzi.accountmicroservice.model.Account$GenderEnum`, problem: Unexpected value 'male'"));
    }

    // Delete Account Success
    @Test
    void testDeleteAccountSuccess() throws Exception{
        doNothing().when(this.accountsService).remove(accountId);

        this.mockMvc.perform(delete("/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

    }

    @Test
    void testDeleteAccountFailed_AccountNotFound() throws Exception{
        doThrow(new AccountNotFoundException(accountId)).when(this.accountsService).remove(accountId);
        this.mockMvc.perform(delete("/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Account with id 1 not found!"));
    }

    @Test
    void testDeleteAccountFailed_AccountIdBadRequest() throws Exception{
        //doThrow(TypeMismatchException.class).when(this.accountsService).remove(accountId);
        this.mockMvc.perform(delete("/accounts/rgfd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'; For input string: \"rgfd\""));
    }

    // Update Account
    @Test
    void testUpdateFullAccountSuccess() throws Exception{
        Account oldAccount = new Account("mario.rossi@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        oldAccount.setName("Mario");
        oldAccount.setSurname("Rossi");

        Account savedAccount = new Account("mario.rossi@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32");
        savedAccount.setName("Mario");
        savedAccount.setSurname("Rossi");
        savedAccount.setId(accountId);

        AccountPatch accountToUpdate = new AccountPatch();
        //account con campi aggiornati
        accountToUpdate.setName("Marietta");
        accountToUpdate.setSurname("Verdi");
        accountToUpdate.setGender(AccountPatch.GenderEnum.FEMALE);
        accountToUpdate.setPassword("hhh3h!d2h234h4");

        //account che mi aspetto di ricevere
        Account updatedAccount = new Account(
                oldAccount.getEmail(),
                oldAccount.getBirthdate(),
                Account.GenderEnum.fromValue(accountToUpdate.getGender().toString()),
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
    void testUpdateAccountPasswordSuccess() throws Exception{
        Account oldAccount = new Account("mario.rossi@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        oldAccount.setName("Mario");
        oldAccount.setSurname("Rossi");

        Account savedAccount = new Account("mario.rossi@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32");
        savedAccount.setName("Mario");
        savedAccount.setSurname("Rossi");
        savedAccount.setId(accountId);

        AccountPatch accountToUpdate = new AccountPatch();
        //account con campi aggiornati

        accountToUpdate.setPassword("hhh3h!d2h234h4");

        //account che mi aspetto di ricevere
        Account updatedAccount = new Account(
                oldAccount.getEmail(),
                oldAccount.getBirthdate(),
                oldAccount.getGender(),
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
    void testUpdateAccountFailed_AccountNotFound() throws Exception{
        Account oldAccount = new Account("mario.rossi@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        oldAccount.setName("Mario");
        oldAccount.setSurname("Rossi");
        AccountPatch accountToUpdate = new AccountPatch();

        //account con campi aggiornati
        accountToUpdate.setName(oldAccount.getName());
        accountToUpdate.setSurname(oldAccount.getSurname());
        accountToUpdate.setGender(AccountPatch.GenderEnum.valueOf(oldAccount.getGender().toString()));
        accountToUpdate.setPassword("hhh3h!d2h234h4");


        //converto l'account che voglio aggiornare in formato json
        String accountToUpdateJson = this.objectMapper.writeValueAsString(accountToUpdate);

        when(this.accountsService.update(accountId, accountToUpdate)).thenThrow(new AccountNotFoundException(1L));

        this.mockMvc.perform(patch("/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(accountToUpdateJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Account with id 1 not found!"));
    }

    @Test
    void testUpdateAccountFailed_AccountIdBadRequest() throws Exception{
        Account oldAccount = new Account("mario.rossi@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        oldAccount.setName("Mario");
        oldAccount.setSurname("Rossi");
        AccountPatch accountToUpdate = new AccountPatch();

        //account con campi aggiornati
        accountToUpdate.setName(oldAccount.getName());
        accountToUpdate.setSurname(oldAccount.getSurname());
        accountToUpdate.setGender(AccountPatch.GenderEnum.valueOf(oldAccount.getGender().toString()));
        accountToUpdate.setPassword("hhh3h!d2h234h4");


        //converto l'account che voglio aggiornare in formato json
        String accountToUpdateJson = this.objectMapper.writeValueAsString(accountToUpdate);

        this.mockMvc.perform(patch("/accounts/rr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(accountToUpdateJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'; For input string: \"rr\""));
    }

    @Test
    void testUpdateAccountFailed_InvalidFields_NameSurnamePassword() throws Exception{
        Account oldAccount = new Account("mario.rossi@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        oldAccount.setName("Mario");
        oldAccount.setSurname("Rossi");
        AccountPatch accountToUpdate = new AccountPatch();

        //account con campi aggiornati
        accountToUpdate.setName("M4");
        accountToUpdate.setSurname("R5");
        accountToUpdate.setGender(AccountPatch.GenderEnum.valueOf(oldAccount.getGender().toString()));
        accountToUpdate.setPassword("hhhvoadsdfsdf");


        //converto l'account che voglio aggiornare in formato json
        String accountToUpdateJson = this.objectMapper.writeValueAsString(accountToUpdate);

        List<String> errors = new ArrayList<>();
        errors.add("password must match \"^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$\"");
        errors.add("name must match \"^[a-zA-Z]+$\"");
        errors.add("surname must match \"^[a-zA-Z]+$\"");


        this.mockMvc.perform(patch("/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(accountToUpdateJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error[0]").value(in(errors)))
                .andExpect(jsonPath("$.error[1]").value(in(errors)))
                .andExpect(jsonPath("$.error[2]").value(in(errors)));
    }

    @Test
    void testUpdateAccountFailed_InvalidGender() throws Exception{
        Account oldAccount = new Account("mario.rossi@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        oldAccount.setName("Mario");
        oldAccount.setSurname("Rossi");
        AccountPatch accountToUpdate = new AccountPatch();

        //account con campi aggiornati

        //accountToUpdate.setGender(AccountPatch.GenderEnum.valueOf(oldAccount.getGender().toString()));

        //converto l'account che voglio aggiornare in formato json
        JsonNode jsonNode = this.objectMapper.readTree(this.objectMapper.writeValueAsString(accountToUpdate));
        ((ObjectNode) jsonNode).put("gender","female");
        String accountToUpdateJson = this.objectMapper.writeValueAsString(jsonNode);

        this.mockMvc.perform(patch("/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(accountToUpdateJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("JSON parse error: Cannot construct instance of `com.sergiostefanizzi.accountmicroservice.model.AccountPatch$GenderEnum`, problem: Unexpected value 'female'"));
    }

    @Test
    void testUpdateAccountFailed_Size() throws Exception{
        Account oldAccount = new Account("mario.rossi@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        oldAccount.setName("Mario");
        oldAccount.setSurname("Rossi");
        AccountPatch accountToUpdate = new AccountPatch();

        //account con campi aggiornati
        accountToUpdate.setName("M");
        accountToUpdate.setSurname("R");
        accountToUpdate.setPassword("h3!");


        //converto l'account che voglio aggiornare in formato json
        String accountToUpdateJson = this.objectMapper.writeValueAsString(accountToUpdate);

        List<String> errors = new ArrayList<>();
        errors.add("password must match \"^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$\"");
        errors.add("password size must be between 8 and 255");
        errors.add("name size must be between 2 and 50");
        errors.add("surname size must be between 2 and 50");


        this.mockMvc.perform(patch("/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(accountToUpdateJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error[0]").value(in(errors)))
                .andExpect(jsonPath("$.error[1]").value(in(errors)))
                .andExpect(jsonPath("$.error[2]").value(in(errors)))
                .andExpect(jsonPath("$.error[3]").value(in(errors)));
    }

}