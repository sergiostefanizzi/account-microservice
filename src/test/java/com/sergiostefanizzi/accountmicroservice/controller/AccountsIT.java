package com.sergiostefanizzi.accountmicroservice.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.AccountPatch;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Slf4j
public class AccountsIT {
    @LocalServerPort
    private int port;
    @Autowired
    private Keycloak keycloak;

    private String baseUrl = "http://localhost";

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ObjectMapper objectMapper;
    private Account newAccount;
    private Account savedAccountToDelete;
    private Account savedAccountToUpdate;
    private Account savedAccountLoginError;



    @BeforeEach
    void setUp(){
        this.baseUrl = this.baseUrl + ":" +port+ "/accounts";

        this.newAccount = new Account("mario.rossi@gmail.com",
                LocalDate.of(1990,4,4),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        this.newAccount.setName("Mario");
        this.newAccount.setSurname("Rossi");

        this.savedAccountToDelete = new Account("pinco.delete@gmail.com",
                LocalDate.of(1970,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        this.savedAccountToDelete.setName("Pinco");
        this.savedAccountToDelete.setSurname("Pallino");
        this.savedAccountToDelete.setId("0a7c3c06-7e77-4473-9111-66a9aa410e9c");

        this.savedAccountToUpdate = new Account("pinco.update@gmail.com",
                LocalDate.of(1970,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        this.savedAccountToUpdate.setName("Pinco");
        this.savedAccountToUpdate.setSurname("Pallino");
        this.savedAccountToUpdate.setId("7e5b9fc2-f31c-4120-aaf8-c1a208179cdd");


        this.savedAccountLoginError = new Account("pinco.login.error@gmail.com",
                LocalDate.of(1970,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        this.savedAccountLoginError.setName("Pinco");
        this.savedAccountLoginError.setSurname("Pallino");
        this.savedAccountLoginError.setId("5c62d672-cfa5-4ebf-a9c3-1622198c69b");
    }

    private String getAccessToken(Account account) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String loginBody = "grant_type=password&client_id=accounts-micro&username="+account.getEmail()+"&password="+account.getPassword();
        HttpEntity<String> request = new HttpEntity<>(loginBody,headers);

        ResponseEntity<String> responseLogin = this.testRestTemplate.exchange(
                "http://localhost:8082/realms/social-accounts/protocol/openid-connect/token",
                HttpMethod.POST,
                request,
                String.class);


        assertEquals(HttpStatus.OK, responseLogin.getStatusCode());
        JsonNode node = this.objectMapper.readTree(responseLogin.getBody());
        String accessToken = node.get("access_token").asText();
        log.info("Access token = "+accessToken);
        return accessToken;
    }

    private void restoreUpdatedAccount(AccountPatch accountPatch) throws JsonProcessingException {
        this.savedAccountToUpdate.setName(accountPatch.getName());
        this.savedAccountToUpdate.setSurname(accountPatch.getSurname());
        this.savedAccountToUpdate.setGender(Account.GenderEnum.valueOf(accountPatch.getGender().toString()));
        this.savedAccountToUpdate.setPassword(accountPatch.getPassword());
        String accessToken = getAccessToken(this.savedAccountToUpdate);

        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setBearerAuth(accessToken);

        AccountPatch accountToUpdate = new AccountPatch();
        accountToUpdate.setName("Pinco");
        accountToUpdate.setSurname("Pallino");
        accountToUpdate.setGender(AccountPatch.GenderEnum.MALE);
        accountToUpdate.setPassword("dshjdfkdjsf32!");

        HttpEntity<AccountPatch> request = new HttpEntity<>(accountToUpdate, updateHeaders);
        ResponseEntity<Account> response = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}",
                HttpMethod.PATCH,
                request,
                Account.class,
                this.savedAccountToUpdate.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(Account.class, response.getBody());
        Account updatedAccount = response.getBody();
        assertEquals(accountToUpdate.getName(),updatedAccount.getName());
        assertEquals(accountToUpdate.getSurname(),updatedAccount.getSurname());
        assertEquals(accountToUpdate.getGender().toString(),updatedAccount.getGender().toString());

        // visualizzo il profilo salvato
        log.info(updatedAccount.toString());

        this.savedAccountToUpdate.setName(accountToUpdate.getName());
        this.savedAccountToUpdate.setSurname(accountToUpdate.getSurname());
        this.savedAccountToUpdate.setGender(Account.GenderEnum.valueOf(accountToUpdate.getGender().toString()));
        this.savedAccountToUpdate.setPassword(accountToUpdate.getPassword());

    }

    private void deletedUser(Account account) {
        UserResource userResource = this.keycloak.realm("social-accounts")
                .users()
                .get(account.getId());
        UserRepresentation userRepresentation = userResource
                .toRepresentation();

        userRepresentation.setEnabled(false);
        userRepresentation.setEmailVerified(false);
        userResource
                .update(userRepresentation);
    }

    private void restoreDeletedUser(Account account) {
        UserResource userResource = this.keycloak.realm("social-accounts")
                .users()
                .get(account.getId());
        UserRepresentation userRepresentation = userResource
                .toRepresentation();

        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(true);
        userResource
                .update(userRepresentation);
    }

    private void invalidateEmail(Account account) {
        UserResource userResource = this.keycloak.realm("social-accounts")
                .users()
                .get(account.getId());
        UserRepresentation userRepresentation = userResource
                .toRepresentation();

        userRepresentation.setEmailVerified(false);
        userResource
                .update(userRepresentation);
    }

    private void validateEmail(Account account) {
        UserResource userResource = this.keycloak.realm("social-accounts")
                .users()
                .get(account.getId());
        UserRepresentation userRepresentation = userResource
                .toRepresentation();

        userRepresentation.setEmailVerified(true);
        userResource
                .update(userRepresentation);
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

        // visualizzo il profilo salvato
        log.info(savedAccount.toString());

        this.keycloak.realm("social-accounts").users().get(this.newAccount.getId()).remove();
    }

    @Test
    void testAddAccountMissing_Name_Surname_Then_201(){
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

        // visualizzo il profilo salvato
        log.info(savedAccount.toString());

        this.keycloak.realm("social-accounts").users().get(this.newAccount.getId()).remove();
    }

    // Add Account Failed



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

        String error = "Message is not readable";

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

        String error = "Message is not readable";
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
    void testAddAccount_AlreadyCreated_Then_409() throws Exception {
        String error = "Conflict! Account with email "+this.savedAccountToDelete.getEmail()+" already created!";
        this.savedAccountToDelete.setId(null);
        HttpEntity<Account> request = new HttpEntity<>(this.savedAccountToDelete);
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

    // Delete Account Success
    @Test
    void
    testDeleteAccountById_Then_204() throws Exception{
        String accessToken = getAccessToken(this.savedAccountToDelete);

        HttpHeaders deleteHeaders = new HttpHeaders();
        deleteHeaders.setBearerAuth(accessToken);

        ResponseEntity<Void> responseDelete = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}",
                HttpMethod.DELETE,
                new HttpEntity<>(deleteHeaders),
                Void.class,
                this.savedAccountToDelete.getId());
        assertEquals(HttpStatus.NO_CONTENT, responseDelete.getStatusCode());

        restoreDeletedUser(this.savedAccountToDelete);
    }



    // Delete Account Failed


    @Test
    void testDeleteAccountById_Then_401(){
        ResponseEntity<String> responseDelete = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                String.class,
                this.savedAccountLoginError.getId());
        assertEquals(HttpStatus.UNAUTHORIZED, responseDelete.getStatusCode());

    }
    @Test
    void testDeleteAccountById_Then_403() throws Exception{
        String accessToken = getAccessToken(this.savedAccountToDelete);

        HttpHeaders deleteHeaders = new HttpHeaders();
        deleteHeaders.setBearerAuth(accessToken);
        ResponseEntity<String> responseDelete = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}",
                HttpMethod.DELETE,
                new HttpEntity<>(deleteHeaders),
                String.class,
                this.savedAccountLoginError.getId());
        JsonNode node = this.objectMapper.readTree(responseDelete.getBody());
        assertEquals(HttpStatus.FORBIDDEN, responseDelete.getStatusCode());
        assertEquals("Forbidden: Account with id "+this.savedAccountToDelete.getId()+" can not perform this action", node.get("error").asText());
        log.info("Error --> "+node.get("error").asText());
    }

    @Test
    void testDeleteAccountById_EmailNotValidated_Then_403() throws Exception{
        invalidateEmail(this.savedAccountToDelete);
        String accessToken = getAccessToken(this.savedAccountToDelete);

        HttpHeaders deleteHeaders = new HttpHeaders();
        deleteHeaders.setBearerAuth(accessToken);
        ResponseEntity<String> responseDelete = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}",
                HttpMethod.DELETE,
                new HttpEntity<>(deleteHeaders),
                String.class,
                this.savedAccountToDelete.getId());
        JsonNode node = this.objectMapper.readTree(responseDelete.getBody());
        assertEquals(HttpStatus.FORBIDDEN, responseDelete.getStatusCode());
        assertEquals("Account's email with id "+this.savedAccountToDelete.getId()+" is not validated", node.get("error").asText());
        log.info("Error --> "+node.get("error").asText());

        validateEmail(this.savedAccountToDelete);
    }

    @Test
    void testDeleteAccountById_Then_404() throws Exception{
        String accessToken = getAccessToken(this.savedAccountToDelete);
        deletedUser(this.savedAccountToDelete);

        HttpHeaders deleteHeaders = new HttpHeaders();
        deleteHeaders.setBearerAuth(accessToken);
        ResponseEntity<String> responseDelete = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}",
                HttpMethod.DELETE,
                new HttpEntity<>(deleteHeaders),
                String.class,
                this.savedAccountToDelete.getId());
        JsonNode node = this.objectMapper.readTree(responseDelete.getBody());
        assertEquals(HttpStatus.NOT_FOUND, responseDelete.getStatusCode());
        assertEquals("Account with id "+this.savedAccountToDelete.getId()+" not found!", node.get("error").asText());
        log.info("Error --> "+node.get("error").asText());

        restoreDeletedUser(this.savedAccountToDelete);
    }



    // Update Account SUCCESS

    @Test
    void testUpdateAccountBy_Then_200() throws Exception{
        String accessToken = getAccessToken(this.savedAccountToUpdate);
        
        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setBearerAuth(accessToken);
        
        AccountPatch accountToUpdate = new AccountPatch();
        accountToUpdate.setName("Nuovonome");
        accountToUpdate.setSurname("Nuovocognome");
        accountToUpdate.setGender(AccountPatch.GenderEnum.FEMALE);
        accountToUpdate.setPassword("NewPassword34#");

        HttpEntity<AccountPatch> request = new HttpEntity<>(accountToUpdate, updateHeaders);
        ResponseEntity<Account> response = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}",
                HttpMethod.PATCH,
                request,
                Account.class,
                this.savedAccountToUpdate.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(Account.class, response.getBody());
        Account updatedAccount = response.getBody();
        assertEquals(accountToUpdate.getName(),updatedAccount.getName());
        assertEquals(accountToUpdate.getSurname(),updatedAccount.getSurname());
        assertEquals(accountToUpdate.getGender().toString(),updatedAccount.getGender().toString());

        // visualizzo il profilo salvato
        log.info(updatedAccount.toString());

        restoreUpdatedAccount(accountToUpdate);
    }

    // Update Account Failed
    @Test
    void testUpdateAccountById_Invalid_NameSurnamePassword_Then_400() throws Exception{
        String accessToken = getAccessToken(this.savedAccountToUpdate);

        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setBearerAuth(accessToken);

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


        HttpEntity<AccountPatch> request = new HttpEntity<>(accountPatchToUpdate, updateHeaders);
        ResponseEntity<String> response = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}",
                HttpMethod.PATCH,
                request,
                String.class,
                this.savedAccountToUpdate.getId());

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
        String accessToken = getAccessToken(this.savedAccountToUpdate);

        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setBearerAuth(accessToken);
        updateHeaders.setContentType(MediaType.APPLICATION_JSON);

        AccountPatch accountPatchToUpdate = new AccountPatch();

        JsonNode jsonNode = this.objectMapper.readTree(this.objectMapper.writeValueAsString(accountPatchToUpdate));
        ((ObjectNode) jsonNode).put("gender","female");
        String accountToUpdateJson = this.objectMapper.writeValueAsString(jsonNode);

        HttpEntity<String> request = new HttpEntity<>(accountToUpdateJson,updateHeaders);
        ResponseEntity<String> response = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}",
                HttpMethod.PATCH,
                request,
                String.class,
                this.savedAccountToUpdate.getId());
        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Message is not readable", node.get("error").asText());
        log.info("Error --> " + node.get("error").asText());
    }

    @Test
    void testUpdateAccountById_FieldsSizeError_Then_400() throws Exception{
        String accessToken = getAccessToken(this.savedAccountToUpdate);

        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setBearerAuth(accessToken);
        updateHeaders.setContentType(MediaType.APPLICATION_JSON);

        //account con campi aggiornati
        AccountPatch accountPatchToUpdate = new AccountPatch();
        accountPatchToUpdate.setName("M");
        accountPatchToUpdate.setSurname("R");
        accountPatchToUpdate.setPassword("h3!");

        List<String> errors = asList("password must match \"^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$\"",
                "password size must be between 8 and 255",
                "name size must be between 2 and 50",
                "surname size must be between 2 and 50");


        HttpEntity<AccountPatch> request = new HttpEntity<>(accountPatchToUpdate,updateHeaders);
        ResponseEntity<String> response = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}",
                HttpMethod.PATCH,
                request,
                String.class,
                this.savedAccountToUpdate.getId());
        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errors.size() ,node.get("error").size());
        for (JsonNode objNode : node.get("error")) {
            assertTrue(errors.contains(objNode.asText()));
            log.info("Error -> "+objNode.asText());
        }
    }

    @Test
    void testUpdateAccountById_Then_401() throws Exception{
        AccountPatch accountPatchToUpdate = new AccountPatch();

        HttpEntity<AccountPatch> request = new HttpEntity<>(accountPatchToUpdate);
        ResponseEntity<String> response = this.testRestTemplate.exchange(
                this.baseUrl + "/{accountId}",
                HttpMethod.PATCH,
                request,
                String.class,
                this.savedAccountLoginError.getId());

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testUpdateAccountById_Then_403() throws Exception{
        String accessToken = getAccessToken(this.savedAccountToUpdate);

        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setBearerAuth(accessToken);

        AccountPatch accountPatchToUpdate = new AccountPatch();

        HttpEntity<AccountPatch> request = new HttpEntity<>(accountPatchToUpdate, updateHeaders);
        ResponseEntity<String> response = this.testRestTemplate.exchange(
                this.baseUrl + "/{accountId}",
                HttpMethod.PATCH,
                request,
                String.class,
                this.savedAccountLoginError.getId());

        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Forbidden: Account with id "+this.savedAccountToUpdate.getId()+" can not perform this action", node.get("error").asText());
        log.info("Error --> " + node.get("error").asText());
    }

    @Test
    void testUpdateAccountById_EmailNotValidated_Then_403() throws Exception{
        invalidateEmail(this.savedAccountToUpdate);
        String accessToken = getAccessToken(this.savedAccountToUpdate);

        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setBearerAuth(accessToken);

        AccountPatch accountPatchToUpdate = new AccountPatch();

        HttpEntity<AccountPatch> request = new HttpEntity<>(accountPatchToUpdate, updateHeaders);
        ResponseEntity<String> response = this.testRestTemplate.exchange(
                this.baseUrl + "/{accountId}",
                HttpMethod.PATCH,
                request,
                String.class,
                this.savedAccountToUpdate.getId());

        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Account's email with id "+this.savedAccountToUpdate.getId()+" is not validated", node.get("error").asText());
        log.info("Error --> "+node.get("error").asText());
        validateEmail(this.savedAccountToUpdate);
    }


    @Test
    void testUpdateAccountById_Then_404() throws Exception{
        String accessToken = getAccessToken(this.savedAccountToUpdate);
        deletedUser(this.savedAccountToUpdate);
        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setBearerAuth(accessToken);

        AccountPatch accountPatchToUpdate = new AccountPatch();

        HttpEntity<AccountPatch> request = new HttpEntity<>(accountPatchToUpdate, updateHeaders);
        ResponseEntity<String> response = this.testRestTemplate.exchange(
                this.baseUrl + "/{accountId}",
                HttpMethod.PATCH,
                request,
                String.class,
                this.savedAccountToUpdate.getId());

        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Account with id "+this.savedAccountToUpdate.getId()+" not found!", node.get("error").asText());
        log.info("Error --> "+node.get("error").asText());
        restoreDeletedUser(this.savedAccountToUpdate);
    }


/*
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
