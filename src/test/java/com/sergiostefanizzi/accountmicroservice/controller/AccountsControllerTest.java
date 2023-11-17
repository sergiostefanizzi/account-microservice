package com.sergiostefanizzi.accountmicroservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.AccountPatch;
import com.sergiostefanizzi.accountmicroservice.service.AccountsService;
import com.sergiostefanizzi.accountmicroservice.service.KeycloakService;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AccountAlreadyCreatedException;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AccountNotFoundException;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.ActionForbiddenException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest(AccountsController.class)
//blocca spring security nei test
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Slf4j
class AccountsControllerTest {

    @MockBean
    private AccountsService accountsService;
    @MockBean
    private KeycloakService keycloakService;
    @MockBean
    private SecurityContext securityContext;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private JwtAuthenticationToken jwtAuthenticationToken;
    private Account newAccount;
    private Account savedAccount;
    private AccountPatch accountToUpdate;
    private final String accountId = UUID.randomUUID().toString();
    private final String invalidId = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);

        this.newAccount = new Account("pinco.pallino@gmail.com",
                LocalDate.of(1990,4,4),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        this.newAccount.setName("Pinco");
        this.newAccount.setSurname("Pallino");


        this.savedAccount = new Account(this.newAccount.getEmail(),
                this.newAccount.getBirthdate(),
                this.newAccount.getGender(),
                this.newAccount.getPassword());
        this.savedAccount.setName(this.newAccount.getName());
        this.savedAccount.setSurname(this.newAccount.getSurname());
        this.savedAccount.setId(this.accountId);


        Map<String, Object> headers = new HashMap<>();
        headers.put("alg","HS256");
        headers.put("typ","JWT");
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub",this.savedAccount.getId());
        Jwt jwt = new Jwt("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
                Instant.now(),
                Instant.MAX,
                headers,
                claims);

        this.jwtAuthenticationToken = new JwtAuthenticationToken(jwt);


        this.accountToUpdate = new AccountPatch();
        this.accountToUpdate.setName("Pinchetta");
        this.accountToUpdate.setSurname("Pallinetta");
        this.accountToUpdate.setGender(AccountPatch.GenderEnum.FEMALE);
        this.accountToUpdate.setPassword("43hg434j5g4!");


    }

    @AfterEach
    void tearDown() {
    }

    // Add account Success


    @Test
    void testAddAccount_Then_201() throws Exception {
        this.savedAccount.setPassword(null);
        String newAccountJson = this.objectMapper.writeValueAsString(this.newAccount);

        when(this.accountsService.save(any(Account.class))).thenReturn(this.savedAccount);

        MvcResult result = this.mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccountJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.*", hasSize(6)))
                .andExpect(jsonPath("$.id").value(this.savedAccount.getId()))
                .andExpect(jsonPath("$.email").value(this.savedAccount.getEmail()))
                .andExpect(jsonPath("$.name").value(this.savedAccount.getName()))
                .andExpect(jsonPath("$.surname").value(this.savedAccount.getSurname()))
                .andExpect(jsonPath("$.birthdate").value(this.savedAccount.getBirthdate().toString()))
                .andExpect(jsonPath("$.gender").value(this.savedAccount.getGender().toString()))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();
        // salvo risposta in result per visualizzarla
        String resultAsString = result.getResponse().getContentAsString();
        Account accountResult = this.objectMapper.readValue(resultAsString, Account.class);

        log.info(accountResult.toString());
    }

    @Test
    void testAddAccountMissing_Name_Surname_Then_201() throws Exception {
        this.newAccount.setName(null);
        this.newAccount.setSurname(null);
        this.savedAccount.setName(null);
        this.savedAccount.setSurname(null);
        this.savedAccount.setPassword(null);
        String newAccountJson = this.objectMapper.writeValueAsString(this.newAccount);
        when(this.accountsService.save(any(Account.class))).thenReturn(this.savedAccount);

        MvcResult result = this.mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccountJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.*", hasSize(4)))
                .andExpect(jsonPath("$.id").value(this.savedAccount.getId()))
                .andExpect(jsonPath("$.email").value(this.savedAccount.getEmail()))
                .andExpect(jsonPath("$.birthdate").value(this.savedAccount.getBirthdate().toString()))
                .andExpect(jsonPath("$.gender").value(this.savedAccount.getGender().toString()))
                .andReturn();
        // salvo risposta in result per visualizzarla
        String resultAsString = result.getResponse().getContentAsString();
        Account accountResult = this.objectMapper.readValue(resultAsString, Account.class);

        log.info(accountResult.toString());
    }

    // Add Account Failed


    @Test
    void testAddAccount_AlreadyCreated_Then_409() throws Exception{
        String newAccountJson = this.objectMapper.writeValueAsString(this.newAccount);

        when(this.accountsService.save(any(Account.class))).thenThrow(new AccountAlreadyCreatedException(this.newAccount.getEmail()));

        MvcResult result = this.mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newAccountJson)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict! Account with email "+this.newAccount.getEmail()+" already created!"))
                .andReturn();

        String resultAsString = result.getResponse().getContentAsString();
        log.info("Errors\n"+resultAsString);
    }

    @Test
    void testAddAccount_MissingRequiredFields_Then_400() throws Exception {
        List<String> errors = asList(
                "email must not be null",
                "birthdate must not be null",
                "gender must not be null",
                "password must not be null"
                );

        this.newAccount.setEmail(null);
        this.newAccount.setBirthdate(null);
        this.newAccount.setGender(null);
        this.newAccount.setPassword(null);

        String newAccountJson = this.objectMapper.writeValueAsString(this.newAccount);



        MvcResult result = this.mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccountJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(res -> assertTrue(res.getResolvedException() instanceof MethodArgumentNotValidException ))
                .andExpect(jsonPath("$.error[0]").value(in(errors)))
                .andExpect(jsonPath("$.error[1]").value(in(errors)))
                .andExpect(jsonPath("$.error[2]").value(in(errors)))
                .andExpect(jsonPath("$.error[3]").value(in(errors)))
                .andReturn();

        String resultAsString = result.getResponse().getContentAsString();
        log.info("Errors\n"+resultAsString);
    }

    @Test
    void testAddAccount_InvalidFields_Then_400() throws Exception {
        List<String> errors = asList("email must be a well-formed email address",
                "password must match \"^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$\"",
                "name must match \"^[a-zA-Z]+$\"",
                "surname must match \"^[a-zA-Z]+$\"");

        this.newAccount.setEmail("pinco.pallinogmail.com");
        this.newAccount.setPassword("dshjdfkdjsf32");
        this.newAccount.setName("Pinco3");
        this.newAccount.setSurname("Pallino3");

        String newAccountJson = this.objectMapper.writeValueAsString(this.newAccount);

        MvcResult result = this.mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccountJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(res -> assertTrue(res.getResolvedException() instanceof MethodArgumentNotValidException ))
                .andExpect(jsonPath("$.error[0]").value(in(errors)))
                .andExpect(jsonPath("$.error[1]").value(in(errors)))
                .andExpect(jsonPath("$.error[2]").value(in(errors)))
                .andExpect(jsonPath("$.error[3]").value(in(errors)))
                .andReturn();

        String resultAsString = result.getResponse().getContentAsString();
        log.info("Errors\n"+resultAsString);

    }

    @Test
    void testAddAccount_FieldSize_Then_400() throws Exception {
        List<String> errors = asList("email must be a well-formed email address",
                "email size must be between 3 and 320",
                "password must match \"^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$\"",
                "password size must be between 8 and 255",
                "name size must be between 2 and 50",
                "surname size must be between 2 and 50");

        this.newAccount.setEmail("p@");
        this.newAccount.setPassword("ds3!");
        this.newAccount.setName("P");
        this.newAccount.setSurname("P");

        String newAccountJson = this.objectMapper.writeValueAsString(newAccount);

        MvcResult result = this.mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccountJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(res -> assertTrue(res.getResolvedException() instanceof MethodArgumentNotValidException ))
                .andExpect(jsonPath("$.error[0]").value(in(errors)))
                .andExpect(jsonPath("$.error[1]").value(in(errors)))
                .andExpect(jsonPath("$.error[2]").value(in(errors)))
                .andExpect(jsonPath("$.error[3]").value(in(errors)))
                .andExpect(jsonPath("$.error[4]").value(in(errors)))
                .andExpect(jsonPath("$.error[5]").value(in(errors)))
                .andReturn();
        String resultAsString = result.getResponse().getContentAsString();
        log.info("Errors\n"+resultAsString);
    }

    // Add Account Failed BIRTHDATE

    @Test
    void testAddAccount_Birthdate_TypeError_Then_400() throws Exception {
        JsonNode jsonNode = this.objectMapper.readTree(this.objectMapper.writeValueAsString(this.newAccount));
        ((ObjectNode) jsonNode).put("birthdate","202308-05");
        String newAccountJson = this.objectMapper.writeValueAsString(jsonNode);

        MvcResult result = this.mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccountJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(res -> assertTrue(res.getResolvedException() instanceof HttpMessageNotReadableException))
                .andExpect(jsonPath("$.error").value("Message is not readable"))
                .andReturn();
        String resultAsString = result.getResponse().getContentAsString();
        log.info("Errors\n"+resultAsString);
    }

    @Test
    void testAddAccount_Birthdate_UnderAge_Then_400() throws Exception {
        this.newAccount.setBirthdate(LocalDate.of(2023,1,2));

        String newAccountJson = this.objectMapper.writeValueAsString(this.newAccount);

        MvcResult result = this.mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccountJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(res -> assertTrue(res.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.error").value("birthdate is not valid! The user must be an adult"))
                .andReturn();
        String resultAsString = result.getResponse().getContentAsString();
        log.info("Errors\n"+resultAsString);
    }

    // Add Account Failed GENDER

    @Test
    void testAddAccount_Gender_TypeError_Then_400() throws Exception {
        JsonNode jsonNode = this.objectMapper.readTree(this.objectMapper.writeValueAsString(this.newAccount));
        ((ObjectNode) jsonNode).put("gender","male");
        String newAccountJson = this.objectMapper.writeValueAsString(jsonNode);

        MvcResult result = this.mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccountJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(res -> assertTrue(res.getResolvedException() instanceof HttpMessageNotReadableException))
                .andExpect(jsonPath("$.error").value("Message is not readable"))
                .andReturn();

        String resultAsString = result.getResponse().getContentAsString();
        log.info("Errors\n"+resultAsString);
    }

    // Delete Account Success

    @Test
    void testDeleteAccountById_Then_204() throws Exception{
        when(this.keycloakService.checkActiveById(anyString())).thenReturn(true);

        when(this.securityContext.getAuthentication()).thenReturn(this.jwtAuthenticationToken);

        this.mockMvc.perform(delete("/accounts/{accountId}",this.savedAccount.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

    }

    // Delete Account Failed


    @Test
    void testDeleteAccountById_Then_403() throws Exception{

        when(this.keycloakService.checkActiveById(anyString())).thenReturn(true);
        when(this.securityContext.getAuthentication()).thenReturn(this.jwtAuthenticationToken);

        MvcResult result = this.mockMvc.perform(delete("/accounts/{accountId}",this.invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(res -> assertTrue(res.getResolvedException() instanceof ActionForbiddenException))
                .andExpect(jsonPath("$.error").value("Forbidden: Account with id "+this.jwtAuthenticationToken.getToken().getClaim("sub")+" can not perform this action"))
                .andReturn();
        String resultAsString = result.getResponse().getContentAsString();
        log.info("Errors\n"+resultAsString);
    }

    @Test
    void testDeleteAccountById_Then_404() throws Exception{
        when(this.securityContext.getAuthentication()).thenReturn(this.jwtAuthenticationToken);
        when(this.keycloakService.checkActiveById(anyString())).thenReturn(false);

        MvcResult result = this.mockMvc.perform(delete("/accounts/{accountId}",this.savedAccount.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(res -> assertTrue(res.getResolvedException() instanceof AccountNotFoundException))
                .andExpect(jsonPath("$.error").value("Account with id "+this.savedAccount.getId()+" not found!"))
                .andReturn();
        String resultAsString = result.getResponse().getContentAsString();
        log.info("Errors\n"+resultAsString);
    }


    // Update Account SUCCESS
    @Test
    void testUpdateAccountBy_Then_200() throws Exception{
        Account updatedAccount = new Account(
                this.savedAccount.getEmail(),
                this.savedAccount.getBirthdate(),
                Account.GenderEnum.fromValue(this.accountToUpdate.getGender().toString()),
                this.accountToUpdate.getPassword()
        );
        updatedAccount.setId(this.savedAccount.getId());
        updatedAccount.setName(this.accountToUpdate.getName());
        updatedAccount.setSurname(this.accountToUpdate.getSurname());

        //converto l'account che voglio aggiornare in formato json
        String accountToUpdateJson = this.objectMapper.writeValueAsString(this.accountToUpdate);

        when(this.keycloakService.checkActiveById(anyString())).thenReturn(true);
        when(this.securityContext.getAuthentication()).thenReturn(this.jwtAuthenticationToken);

        when(this.accountsService.update(anyString(), any(AccountPatch.class))).thenReturn(updatedAccount);

        MvcResult result = this.mockMvc.perform(patch("/accounts/{accountId}",this.savedAccount.getId())
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
                .andExpect(jsonPath("$.password").value(updatedAccount.getPassword()))
                .andReturn();

        String resultAsString = result.getResponse().getContentAsString();
        Account accountResult = this.objectMapper.readValue(resultAsString, Account.class);

        log.info(accountResult.toString());

    }


    // Update Account Failed

    @Test
    void testUpdateAccountById_Invalid_NameSurnamePassword_Then_400() throws Exception{
        this.accountToUpdate.setName("P3");
        this.accountToUpdate.setSurname("P3");
        this.accountToUpdate.setGender(AccountPatch.GenderEnum.FEMALE);
        this.accountToUpdate.setPassword("43hg434j5g4");


        //converto l'account che voglio aggiornare in formato json
        String accountToUpdateJson = this.objectMapper.writeValueAsString(this.accountToUpdate);

        List<String> errors = asList("password must match \"^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$\"",
                "name must match \"^[a-zA-Z]+$\"",
                "surname must match \"^[a-zA-Z]+$\"");

        when(this.keycloakService.checkActiveById(anyString())).thenReturn(true);
        when(this.securityContext.getAuthentication()).thenReturn(this.jwtAuthenticationToken);

        MvcResult result = this.mockMvc.perform(patch("/accounts/{accountId}",this.savedAccount.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(accountToUpdateJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(res -> assertTrue(res.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.error[0]").value(in(errors)))
                .andExpect(jsonPath("$.error[1]").value(in(errors)))
                .andExpect(jsonPath("$.error[2]").value(in(errors)))
                .andReturn();
        String resultAsString = result.getResponse().getContentAsString();
        log.info("Errors\n"+resultAsString);
    }

    @Test
    void testUpdateAccountById_Invalid_Gender_Then_400() throws Exception{
        //converto l'account che voglio aggiornare in formato json
        JsonNode jsonNode = this.objectMapper.readTree(this.objectMapper.writeValueAsString(this.accountToUpdate));
        ((ObjectNode) jsonNode).put("gender","female");
        String accountToUpdateJson = this.objectMapper.writeValueAsString(jsonNode);

        when(this.keycloakService.checkActiveById(anyString())).thenReturn(true);
        when(this.securityContext.getAuthentication()).thenReturn(this.jwtAuthenticationToken);

        MvcResult result = this.mockMvc.perform(patch("/accounts/{accountId}",this.savedAccount.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(accountToUpdateJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(res -> assertTrue(res.getResolvedException() instanceof HttpMessageNotReadableException))
                .andExpect(jsonPath("$.error").value("Message is not readable"))
                .andReturn();

        String resultAsString = result.getResponse().getContentAsString();
        log.info("Errors\n"+resultAsString);
    }

    @Test
    void testUpdateAccountById_FieldsSizeError_Then_400() throws Exception{
        this.accountToUpdate.setName("P");
        this.accountToUpdate.setSurname("P");
        this.accountToUpdate.setGender(AccountPatch.GenderEnum.FEMALE);
        this.accountToUpdate.setPassword("43h!");

        //converto l'account che voglio aggiornare in formato json
        String accountToUpdateJson = this.objectMapper.writeValueAsString(this.accountToUpdate);

        List<String> errors = asList("password must match \"^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$\"",
                "password size must be between 8 and 255",
                "name size must be between 2 and 50",
                "surname size must be between 2 and 50");

        when(this.keycloakService.checkActiveById(anyString())).thenReturn(true);
        when(this.securityContext.getAuthentication()).thenReturn(this.jwtAuthenticationToken);

        MvcResult result = this.mockMvc.perform(patch("/accounts/{accountId}",this.savedAccount.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(accountToUpdateJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(res-> assertTrue(res.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.error[0]").value(in(errors)))
                .andExpect(jsonPath("$.error[1]").value(in(errors)))
                .andExpect(jsonPath("$.error[2]").value(in(errors)))
                .andExpect(jsonPath("$.error[3]").value(in(errors)))
                .andReturn();
        String resultAsString = result.getResponse().getContentAsString();
        log.info("Errors\n"+resultAsString);
    }

    @Test
    void testUpdateAccountById_Then_403() throws Exception{
        //converto l'account che voglio aggiornare in formato json
        String accountToUpdateJson = this.objectMapper.writeValueAsString(this.accountToUpdate);

        when(this.keycloakService.checkActiveById(anyString())).thenReturn(true);
        when(this.securityContext.getAuthentication()).thenReturn(this.jwtAuthenticationToken);

        MvcResult result = this.mockMvc.perform(patch("/accounts/{accountId}",this.invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(accountToUpdateJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(res -> assertTrue(res.getResolvedException() instanceof ActionForbiddenException))
                .andExpect(jsonPath("$.error").value("Forbidden: Account with id "+this.jwtAuthenticationToken.getToken().getClaim("sub")+" can not perform this action"))
                .andReturn();
        String resultAsString = result.getResponse().getContentAsString();
        log.info("Errors\n"+resultAsString);
    }

    @Test
    void testUpdateAccountById_Then_404() throws Exception{
        //converto l'account che voglio aggiornare in formato json
        String accountToUpdateJson = this.objectMapper.writeValueAsString(this.accountToUpdate);
        when(this.securityContext.getAuthentication()).thenReturn(this.jwtAuthenticationToken);
        when(this.keycloakService.checkActiveById(anyString())).thenReturn(false);

        MvcResult result = this.mockMvc.perform(patch("/accounts/{accountId}",this.savedAccount.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(accountToUpdateJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(res -> assertTrue(res.getResolvedException() instanceof AccountNotFoundException))
                .andExpect(jsonPath("$.error").value("Account with id "+this.savedAccount.getId()+" not found!"))
                .andReturn();
        String resultAsString = result.getResponse().getContentAsString();
        log.info("Errors\n"+resultAsString);
    }



/*
    // Account activation SUCCESS
    @Test
    void testActivateAccountById_Then_204() throws Exception{
        this.savedAccountJpa.setValidatedAt(null);
        when(this.keycloakService.checkActiveById(anyString())).thenReturn(true);
        this.mockMvc.perform(put("/accounts/{accountId}",this.savedAccount.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("validation_code", UUID.randomUUID().toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    // Account activation Failed
    @Test
    void testActivateAccountById_NotWellFormatted_Then_400() throws Exception{
        String validationCode = UUID.randomUUID().toString();
        String invalidValidationCode = validationCode.substring(0,validationCode.length()-1);
        this.savedAccountJpa.setValidatedAt(null);
        when(this.keycloakService.checkActiveById(anyString())).thenReturn(true);
        MvcResult result = this.mockMvc.perform(put("/accounts/{accountId}",this.savedAccount.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("validation_code", invalidValidationCode)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(res-> assertTrue(res.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(jsonPath("$.error").value("activateAccountById.validationCode: must match \"^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$\""))
                .andReturn();
        String resultAsString = result.getResponse().getContentAsString();
        log.info("Errors\n"+resultAsString);
    }

    @Test
    void testActivateAccountById_Invalid_Code_Then_400() throws Exception{
        String validationCode = UUID.randomUUID().toString();

        this.savedAccountJpa.setValidatedAt(null);
        when(this.keycloakService.checkActiveById(anyString())).thenReturn(true);
        doThrow(new AccountNotActivedException(this.savedAccount.getId())).when(this.accountsService).active(anyString(), anyString());
        MvcResult result = this.mockMvc.perform(put("/accounts/{accountId}",this.savedAccount.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("validation_code", validationCode)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(res -> assertTrue(res.getResolvedException() instanceof AccountNotActivedException))
                .andExpect(jsonPath("$.error").value("Error during activation of the account!"))
                .andReturn();
        String resultAsString = result.getResponse().getContentAsString();
        log.info("Errors\n"+resultAsString);

    }

    @Test
    void testActivateAccountById_AccountAlreadyActivated_Then_400() throws Exception{
        String validationCode = UUID.randomUUID().toString();


        when(this.keycloakService.checkActiveById(anyString())).thenReturn(true);
        doThrow(new AccountAlreadyActivatedException(this.savedAccount.getId())).when(this.accountsService).active(anyString(), anyString());
        MvcResult result = this.mockMvc.perform(put("/accounts/{accountId}",this.savedAccount.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("validation_code", validationCode)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(res -> assertTrue(res.getResolvedException() instanceof AccountAlreadyActivatedException))
                .andExpect(jsonPath("$.error").value("Account with id "+this.savedAccount.getId()+" already activated!"))
                .andReturn();
        String resultAsString = result.getResponse().getContentAsString();
        log.info("Errors\n"+resultAsString);

    }

 */



}