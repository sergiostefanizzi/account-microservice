package com.sergiostefanizzi.accountmicroservice.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.AccountJpa;
import com.sergiostefanizzi.accountmicroservice.model.AccountPatch;
import com.sergiostefanizzi.accountmicroservice.repository.AccountsRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@EnableAutoConfiguration(exclude = { SecurityAutoConfiguration.class})
@ActiveProfiles("test")
@Slf4j
public class AccountsIT {
    @LocalServerPort
    private int port;

    private String baseUrl = "http://localhost";

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AccountsRepository accountsRepository;
    private Account newAccount;
    private Account savedAccount1;

/*
    @BeforeEach
    void setUp() {
        this.baseUrl = this.baseUrl + ":" +port+ "/accounts";

        this.newAccount = new Account("mario.rossi@gmail.com",
                LocalDate.of(1990,4,4),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        this.newAccount.setName("Mario");
        this.newAccount.setSurname("Rossi");

        this.savedAccount1 = new Account("pinco.pallino@gmail.com",
                LocalDate.of(1970,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        this.savedAccount1.setName("Pinco");
        this.savedAccount1.setSurname("Pallino");
        this.savedAccount1.setId(101L);
    }


    // Add account Success
    @Test
    public void testAddAccount_Then_201(){
        HttpEntity<Account> request = new HttpEntity<>(this.newAccount);
        ResponseEntity<Account> response = this.testRestTemplate.exchange(this.baseUrl,
                HttpMethod.POST,
                request,
                Account.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(Account.class, response.getBody());
        Account savedAccount = response.getBody();
        assertNotNull(savedAccount.getId());
        this.newAccount.setPassword(null);
        this.newAccount.setId(savedAccount.getId());
        assertEquals(this.newAccount, savedAccount);

        // visulazzo il profilo salvato
        log.info(savedAccount.toString());
    }

    @Test
    void testAddAccountMissing_Name_Surname_Then_201(){
        this.newAccount.setEmail("mario.rossi2@gmail.com");
        this.newAccount.setName(null);
        this.newAccount.setSurname(null);
        HttpEntity<Account> request = new HttpEntity<>(this.newAccount);
        ResponseEntity<Account> response = this.testRestTemplate.exchange(this.baseUrl,
                HttpMethod.POST,
                request,
                Account.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(Account.class, response.getBody());
        Account savedAccount = response.getBody();
        assertNotNull(savedAccount.getId());
        this.newAccount.setPassword(null);
        this.newAccount.setId(savedAccount.getId());
        assertEquals(this.newAccount, savedAccount);

        // visulazzo il profilo salvato
        log.info(savedAccount.toString());
    }

    // Add Account Failed

    @Test
    void testAddAccount_AlreadyCreated_Then_409() throws Exception {
        String error = "Conflict! Account with email "+this.savedAccount1.getEmail()+" already created!";
        this.savedAccount1.setId(null);
        HttpEntity<Account> request = new HttpEntity<>(this.savedAccount1);
        ResponseEntity<String> response = this.testRestTemplate.exchange(this.baseUrl,
                HttpMethod.POST,
                request,
                String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());

        JsonNode node = this.objectMapper.readTree(response.getBody());
        // In questo caso l'errore NON è un array di dimensione 1
        assertEquals(error ,node.get("error").asText()); // asText() perche' mi dava una stringa tra doppi apici e non riuscivo a fare il confronto
        log.info("Error -> "+node.get("error"));
    }

    @Test
    void testAddAccount_MissingRequiredFields_Then_400() throws  Exception{
        List<String> errors = asList(
                "email must not be null",
                "birthdate must not be null",
                "gender must not be null",
                "password must not be null");

        this.newAccount.setEmail(null);
        this.newAccount.setBirthdate(null);
        this.newAccount.setGender(null);
        this.newAccount.setPassword(null);

        HttpEntity<Account> request = new HttpEntity<>(this.newAccount);
        ResponseEntity<String> response = this.testRestTemplate.exchange(this.baseUrl,
                HttpMethod.POST,
                request,
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        JsonNode node = this.objectMapper.readTree(response.getBody());
        // itero gli errori ottenuti dalla risposta per confrontarli con quelli che mi aspetto di ottenere
        assertEquals(errors.size() ,node.get("error").size());
        for (JsonNode objNode : node.get("error")) {
            assertTrue(errors.contains(objNode.asText()));
            log.info("Error -> "+objNode.asText());
        }

    }

    @Test
    void testAddAccount_FieldSize_Then_400() throws Exception{

        List<String> errors = asList(
                "email must be a well-formed email address",
                "email size must be between 3 and 320",
                "password must match \"^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$\"",
                "password size must be between 8 and 255",
                "name size must be between 2 and 50",
                "surname size must be between 2 and 50");

        this.newAccount.setEmail("p@");
        this.newAccount.setPassword("ds3!");
        this.newAccount.setName("P");
        this.newAccount.setSurname("P");

        HttpEntity<Account> request = new HttpEntity<>(this.newAccount);
        ResponseEntity<String> response = this.testRestTemplate.exchange(this.baseUrl,
                HttpMethod.POST,
                request,
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        JsonNode node = this.objectMapper.readTree(response.getBody());
        // itero gli errori ottenuti dalla risposta per confrontarli con quelli che mi aspetto di ottenere
        assertEquals(errors.size() ,node.get("error").size());
        for (JsonNode objNode : node.get("error")) {
            assertTrue(errors.contains(objNode.asText()));
            log.info("Error -> "+objNode.asText());
        }
    }

    // Add Account Failed BIRTHDATE

    @Test
    void testAddAccount_Birthdate_TypeError_Then_400() throws Exception{
        JsonNode jsonNode = this.objectMapper.readTree(this.objectMapper.writeValueAsString(this.newAccount));
        ((ObjectNode) jsonNode).put("birthdate","202308-05");
        String newAccountJson = this.objectMapper.writeValueAsString(jsonNode);

        String error = "JSON parse error: Cannot deserialize value of type `java.time.LocalDate` from String \"202308-05\": Failed to deserialize java.time.LocalDate: (java.time.format.DateTimeParseException) Text '202308-05' could not be parsed at index 0";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(newAccountJson, headers);

        ResponseEntity<String> response = this.testRestTemplate.exchange(this.baseUrl,
                HttpMethod.POST,
                request,
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        JsonNode node = this.objectMapper.readTree(response.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(error, node.get("error").asText());
        log.info("Error -> "+node.get("error").asText());
    }

    @Test
    void testAddAccount_Birthdate_UnderAge_Then_400() throws Exception{
        this.newAccount.setBirthdate(LocalDate.now().minusYears(1));

        String error = "birthdate is not valid! The user must be an adult";

        HttpEntity<Account> request = new HttpEntity<>(this.newAccount);
        ResponseEntity<String> response = this.testRestTemplate.exchange(this.baseUrl,
                HttpMethod.POST,
                request,
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        JsonNode node = this.objectMapper.readTree(response.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(node.get("error").isArray());
        assertEquals(error, node.get("error").get(0).asText());
        log.info("Error -> "+node.get("error").get(0));
    }

    @Test
    void testAddAccount_Gender_TypeError_Then_400() throws Exception{

        JsonNode jsonNode = this.objectMapper.readTree(this.objectMapper.writeValueAsString(this.newAccount));
        ((ObjectNode) jsonNode).put("gender","male");
        String newAccountJson = this.objectMapper.writeValueAsString(jsonNode);

        String error = "JSON parse error: Cannot construct instance of `com.sergiostefanizzi.accountmicroservice.model.Account$GenderEnum`, problem: Unexpected value 'male'";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(newAccountJson, headers);

        ResponseEntity<String> response = this.testRestTemplate.exchange(this.baseUrl,
                HttpMethod.POST,
                request,
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        JsonNode node = this.objectMapper.readTree(response.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(error, node.get("error").asText());
        log.info("Error -> "+node.get("error").asText());
    }


    // Delete Account Success
    @Test
    void
    testDeleteAccountById_Then_204() throws Exception{
        Long accountToDeleteId = 107L;
        ResponseEntity<Void> responseDelete = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                Void.class,
                accountToDeleteId);
        assertEquals(HttpStatus.NO_CONTENT, responseDelete.getStatusCode());

    }

    // Delete Account Failed
    @Test
    void testDeleteAccountById_Then_404() throws Exception{
        ResponseEntity<String> responseDelete = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                String.class,
                Long.MAX_VALUE);
        JsonNode node = this.objectMapper.readTree(responseDelete.getBody());
        assertEquals(HttpStatus.NOT_FOUND, responseDelete.getStatusCode());
        assertEquals("Account with id "+Long.MAX_VALUE+" not found!", node.get("error").asText());
        log.info("Error --> "+node.get("error").asText());
    }

    @Test
    void testDeleteAccountById_Then_400() throws Exception{
        ResponseEntity<String> response = this.testRestTemplate.exchange(
                this.baseUrl+"/IdNotLong",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                String.class);

        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("ID is not valid!", node.get("error").asText());
        log.info("Error --> "+node.get("error").asText());
    }

    // Update Account SUCCESS

    @Test
    void testUpdateAccountBy_Then_200() throws Exception{
        AccountPatch accountToUpdate = new AccountPatch();
        accountToUpdate.setName("Nuovonome");
        accountToUpdate.setSurname("Nuovocognome");
        accountToUpdate.setGender(AccountPatch.GenderEnum.FEMALE);
        accountToUpdate.setPassword("NewPassword34#");

        HttpEntity<AccountPatch> request = new HttpEntity<>(accountToUpdate);
        ResponseEntity<Account> response = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}",
                HttpMethod.PATCH,
                request,
                Account.class,
                106L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(Account.class, response.getBody());
        Account updatedAccount = response.getBody();
        assertEquals(accountToUpdate.getName(),updatedAccount.getName());
        assertEquals(accountToUpdate.getSurname(),updatedAccount.getSurname());
        assertEquals(accountToUpdate.getGender().toString(),updatedAccount.getGender().toString());


        // visulazzo il profilo salvato
        log.info(updatedAccount.toString());
    }

    // Update Account Failed
    @Test
    void testUpdateAccountById_Then_404() throws Exception{
        AccountPatch accountPatchToUpdate = new AccountPatch();

        HttpEntity<AccountPatch> request = new HttpEntity<>(accountPatchToUpdate);
        ResponseEntity<String> response = this.testRestTemplate.exchange(
                this.baseUrl + "/{accountId}",
                HttpMethod.PATCH,
                request,
                String.class,
                Long.MAX_VALUE);

        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Account with id "+Long.MAX_VALUE+" not found!", node.get("error").asText());
        log.info("Error --> " + node.get("error").asText());
    }

    @Test
    void testUpdateAccountById_Then_400() throws Exception {

        HttpEntity<AccountPatch> request = new HttpEntity<>(new AccountPatch());
        ResponseEntity<String> response = this.testRestTemplate.exchange(
                this.baseUrl + "/IdNotLong",
                HttpMethod.PATCH, request,
                String.class);

        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("ID is not valid!", node.get("error").asText());
        log.info("Error --> " + node.get("error").asText());
    }

    @Test
    void testUpdateAccountById_Invalid_NameSurnamePassword_Then_400() throws Exception{
        AccountPatch accountPatchToUpdate = new AccountPatch();
        accountPatchToUpdate.setName("M4");
        accountPatchToUpdate.setSurname("R5");
        accountPatchToUpdate.setGender(AccountPatch.GenderEnum.valueOf(newAccount.getGender().toString()));
        accountPatchToUpdate.setPassword("hhhvoadsdfsdf");

        //converto l'account che voglio aggiornare in formato json

        List<String> errors = asList(
                "password must match \"^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$\"",
                "name must match \"^[a-zA-Z]+$\"",
                "surname must match \"^[a-zA-Z]+$\"");


        HttpEntity<AccountPatch> request = new HttpEntity<>(accountPatchToUpdate);
        ResponseEntity<String> response = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}",
                HttpMethod.PATCH,
                request,
                String.class,
                106L);

        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errors.size() ,node.get("error").size());
        for (JsonNode objNode : node.get("error")) {
            assertTrue(errors.contains(objNode.asText()));
            log.info("Error -> "+objNode.asText());
        }
    }

    @Test
    void testUpdateAccountById_Invalid_Gender_Then_400() throws Exception{
        AccountPatch accountPatchToUpdate = new AccountPatch();

        JsonNode jsonNode = this.objectMapper.readTree(this.objectMapper.writeValueAsString(accountPatchToUpdate));
        ((ObjectNode) jsonNode).put("gender","female");
        String accountToUpdateJson = this.objectMapper.writeValueAsString(jsonNode);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(accountToUpdateJson,headers);
        ResponseEntity<String> response = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}",
                HttpMethod.PATCH,
                request,
                String.class,
                106L);
        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("JSON parse error: Cannot construct instance of `com.sergiostefanizzi.accountmicroservice.model.AccountPatch$GenderEnum`, problem: Unexpected value 'female'", node.get("error").asText());
        log.info("Error --> " + node.get("error").asText());
    }

    @Test
    void testUpdateAccountById_FieldsSizeError_Then_400() throws Exception{
        //account con campi aggiornati
        AccountPatch accountPatchToUpdate = new AccountPatch();
        accountPatchToUpdate.setName("M");
        accountPatchToUpdate.setSurname("R");
        accountPatchToUpdate.setPassword("h3!");

        List<String> errors = asList("password must match \"^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$\"",
                "password size must be between 8 and 255",
                "name size must be between 2 and 50",
                "surname size must be between 2 and 50");


        HttpEntity<AccountPatch> request = new HttpEntity<>(accountPatchToUpdate);
        ResponseEntity<String> response = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}",
                HttpMethod.PATCH,
                request,
                String.class,
                106L);
        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errors.size() ,node.get("error").size());
        for (JsonNode objNode : node.get("error")) {
            assertTrue(errors.contains(objNode.asText()));
            log.info("Error -> "+objNode.asText());
        }
    }

    // Account activation SUCCESS

    @Test
    void testActivateAccountById_Then_204() throws Exception{
        ResponseEntity<Void> response = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}?validation_code={code}",
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                Void.class,
                108L,
                "d5a84c1e-89b9-40a4-a7a0-1a3f8fbd5472");
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    // Account activation Failed
    @Test
    void testActivateAccountById_NotWellFormatted_Then_400() throws Exception{
        String validationCode = UUID.randomUUID().toString();
        String invalidValidationCode = validationCode.substring(0,validationCode.length()-1);

        ResponseEntity<String> response = this.testRestTemplate.exchange(this.baseUrl+"/{accountId}?validation_code={code}",
                HttpMethod.PUT, HttpEntity.EMPTY,
                String.class,
                108L,
                invalidValidationCode);
        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("activateAccountById.validationCode: must match \"^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$\"", node.get("error").asText());
        log.info("Error --> " + node.get("error").asText());
    }

    @Test
    void testActivateAccountById_Invalid_Code_Then_400() throws Exception{
        String invalidValidationCode = UUID.randomUUID().toString();

        ResponseEntity<String> response = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}?validation_code={code}",
                HttpMethod.PUT, HttpEntity.EMPTY,
                String.class,
                108L,
                invalidValidationCode);
        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error during activation of the account!", node.get("error").asText());
        log.info("Error --> " + node.get("error").asText());
    }

    @Test
    void testActivateAccountById_Account_AlreadyActivated_Then_400() throws Exception{

        ResponseEntity<String> response = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}?validation_code={code}",
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                String.class,
                101L,
                "f13e7cf9-fcb2-4650-9648-4efae38ca4ac");
        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Account with id "+101L+" already activated!", node.get("error").asText());
        log.info("Error --> " + node.get("error").asText());
    }

    @Test
    void testActivateAccountById_AccountNotFound_Then_404() throws Exception{
        String invalidValidationCode = UUID.randomUUID().toString();

        ResponseEntity<String> response = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}?validation_code={code}",
                HttpMethod.PUT, HttpEntity.EMPTY,
                String.class,
                Long.MAX_VALUE,
                invalidValidationCode);
        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Account with id "+Long.MAX_VALUE+" not found!", node.get("error").asText());
        log.info("Error --> " + node.get("error").asText());
    }

 */



}
