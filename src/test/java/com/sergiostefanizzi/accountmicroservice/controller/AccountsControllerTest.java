package com.sergiostefanizzi.accountmicroservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AccountAlreadyCreatedException;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AccountNotActivedException;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.AccountNotFoundException;
import com.sergiostefanizzi.accountmicroservice.controller.exceptions.ValidationCodeNotValidException;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.AccountPatch;
import com.sergiostefanizzi.accountmicroservice.service.AccountsService;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.in;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest(AccountsController.class)
@ActiveProfiles("test")
@Slf4j
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
    void testAddAccount_Then_201() throws Exception {
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
        savedAccount.setName("Mario");
        savedAccount.setSurname("Rossi");
        savedAccount.setId(accountId);

        String newAccountJson = this.objectMapper.writeValueAsString(newAccount);

        when(this.accountsService.save(newAccount)).thenReturn(savedAccount);

        MvcResult result = this.mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON).content(newAccountJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()).andReturn();
        String contentAsString = result.getResponse().getContentAsString();

        Account responseAccount = objectMapper.readValue(contentAsString, Account.class);
        log.info("Resp --> "+responseAccount.getEmail());
    }
    @Test
    void testAddAccountMissing_Name_Surname_Then_201() throws Exception {
        Account newAccount = new Account("mario.rossi@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");

        Account savedAccount = new Account("mario.rossi@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32");
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
    void testAddAccount_AlreadyCreated_Then_409() throws Exception{
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
    void testAddAccount_MissingRequiredFields_Then_400() throws Exception {
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
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException ))
                .andExpect(jsonPath("$.error[0]").value(in(errors)))
                .andExpect(jsonPath("$.error[1]").value(in(errors)))
                .andExpect(jsonPath("$.error[2]").value(in(errors)))
                .andExpect(jsonPath("$.error[3]").value(in(errors)));
    }

    @Test
    void testAddAccount_InvalidFields_Then_400() throws Exception {
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
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException ))
                .andExpect(jsonPath("$.error[0]").value(in(errors)))
                .andExpect(jsonPath("$.error[1]").value(in(errors)))
                .andExpect(jsonPath("$.error[2]").value(in(errors)))
                .andExpect(jsonPath("$.error[3]").value(in(errors)));
    }

    @Test
    void testAddAccount_FieldSize_Then_400() throws Exception {
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
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException ))
                .andExpect(jsonPath("$.error[0]").value(in(errors)))
                .andExpect(jsonPath("$.error[1]").value(in(errors)))
                .andExpect(jsonPath("$.error[2]").value(in(errors)))
                .andExpect(jsonPath("$.error[3]").value(in(errors)))
                .andExpect(jsonPath("$.error[4]").value(in(errors)))
                .andExpect(jsonPath("$.error[5]").value(in(errors)));
    }

    // Add Account Failed BIRTHDATE

    @Test
    void testAddAccount_Birthdate_TypeError_Then_400() throws Exception {
        Account newAccount = new Account("mario.rossi@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        newAccount.setName("Mario");
        newAccount.setSurname("Rossi");

        JsonNode jsonNode = this.objectMapper.readTree(this.objectMapper.writeValueAsString(newAccount));
        ((ObjectNode) jsonNode).put("birthdate","202308-05");
        String newAccountJson = this.objectMapper.writeValueAsString(jsonNode);

        this.mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccountJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof HttpMessageNotReadableException))
                .andExpect(jsonPath("$.error").value("JSON parse error: Cannot deserialize value of type `java.time.LocalDate` from String \"202308-05\": Failed to deserialize java.time.LocalDate: (java.time.format.DateTimeParseException) Text '202308-05' could not be parsed at index 0"));
    }

    @Test
    void testAddAccount_Birthdate_UnderAge_Then_400() throws Exception {
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
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.error").value("birthdate is not valid! The user must be an adult"));
    }

    // Add Account Failed GENDER

    @Test
    void testAddAccount_Gender_TypeError_Then_400() throws Exception {
        Account newAccount = new Account("mario.rossi@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        newAccount.setName("Mario");
        newAccount.setSurname("Rossi");

        JsonNode jsonNode = this.objectMapper.readTree(this.objectMapper.writeValueAsString(newAccount));
        ((ObjectNode) jsonNode).put("gender","male");
        String newAccountJson = this.objectMapper.writeValueAsString(jsonNode);

        this.mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccountJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof HttpMessageNotReadableException))
                .andExpect(jsonPath("$.error").value("JSON parse error: Cannot construct instance of `com.sergiostefanizzi.accountmicroservice.model.Account$GenderEnum`, problem: Unexpected value 'male'"));
    }

    // Delete Account Success
    @Test
    void testDeleteAccountById_Then_204() throws Exception{
        doNothing().when(this.accountsService).remove(accountId);

        this.mockMvc.perform(delete("/accounts/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

    }

    // Delete Account Failed

    @Test
    void testDeleteAccountById_Then_404() throws Exception{
        doThrow(new AccountNotFoundException(accountId)).when(this.accountsService).remove(accountId);

        this.mockMvc.perform(delete("/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof AccountNotFoundException))
                .andExpect(jsonPath("$.error").value("Account with id 1 not found!"));
    }

    @Test
    void testDeleteAccountById_Then_400() throws Exception{
        this.mockMvc.perform(delete("/accounts/rgfd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentTypeMismatchException))
                .andExpect(jsonPath("$.error").value("Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'; For input string: \"rgfd\""));
    }

    // Update Account SUCCESS
    @Test
    void testUpdateAccountBy_Then_200() throws Exception{
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
    void testUpdateAccountById_Password_Then_200() throws Exception{
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

    // Update Account Failed

    @Test
    void testUpdateAccountById_Then_404() throws Exception{
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
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof AccountNotFoundException))
                .andExpect(jsonPath("$.error").value("Account with id 1 not found!"));
    }

    @Test
    void testUpdateAccountById_Then_400() throws Exception{
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
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentTypeMismatchException))
                .andExpect(jsonPath("$.error").value("Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'; For input string: \"rr\""));
    }

    @Test
    void testUpdateAccountById_Invalid_NameSurnamePassword_Then_400() throws Exception{
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
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.error[0]").value(in(errors)))
                .andExpect(jsonPath("$.error[1]").value(in(errors)))
                .andExpect(jsonPath("$.error[2]").value(in(errors)));
    }

    @Test
    void testUpdateAccountById_Invalid_Gender_Then_400() throws Exception{
        Account oldAccount = new Account("mario.rossi@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        oldAccount.setName("Mario");
        oldAccount.setSurname("Rossi");
        AccountPatch accountToUpdate = new AccountPatch();

        //account con campi aggiornati


        //converto l'account che voglio aggiornare in formato json
        JsonNode jsonNode = this.objectMapper.readTree(this.objectMapper.writeValueAsString(accountToUpdate));
        ((ObjectNode) jsonNode).put("gender","female");
        String accountToUpdateJson = this.objectMapper.writeValueAsString(jsonNode);

        this.mockMvc.perform(patch("/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(accountToUpdateJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof HttpMessageNotReadableException))
                .andExpect(jsonPath("$.error").value("JSON parse error: Cannot construct instance of `com.sergiostefanizzi.accountmicroservice.model.AccountPatch$GenderEnum`, problem: Unexpected value 'female'"));
    }

    @Test
    void testUpdateAccountById_FieldsSizeError_Then_400() throws Exception{
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
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.error[0]").value(in(errors)))
                .andExpect(jsonPath("$.error[1]").value(in(errors)))
                .andExpect(jsonPath("$.error[2]").value(in(errors)))
                .andExpect(jsonPath("$.error[3]").value(in(errors)));
    }

    // Account activation SUCCESS
    @Test
    void testActivateAccountById_Then_204() throws Exception{
        String validationCode = UUID.randomUUID().toString();
        doNothing().when(this.accountsService).active(accountId, validationCode);

        this.mockMvc.perform(put("/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("validation_code", validationCode)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    // Account activation Failed
    @Test
    void testActivateAccountById_NotWellFormatted_Then_400() throws Exception{
        String validationCode = UUID.randomUUID().toString();
        String invalidValidationCode = validationCode.substring(0,validationCode.length()-1);
        doNothing().when(this.accountsService).active(accountId, invalidValidationCode);


        this.mockMvc.perform(put("/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("validation_code", invalidValidationCode)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(jsonPath("$.error").value("activateAccountById.validationCode: must match \"^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$\""));
    }
    @Test
    void testActivateAccountById_Invalid_Code_Then_400() throws Exception{
        String validationCode = UUID.randomUUID().toString();

        doThrow(new AccountNotActivedException(accountId)).when(this.accountsService).active(accountId, validationCode);

        this.mockMvc.perform(put("/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("validation_code", validationCode)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof AccountNotActivedException));
    }

}