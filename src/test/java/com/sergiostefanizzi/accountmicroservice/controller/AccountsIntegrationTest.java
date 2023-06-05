package com.sergiostefanizzi.accountmicroservice.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.AccountPatch;
import com.sergiostefanizzi.accountmicroservice.repository.AccountsRepository;
import com.sergiostefanizzi.accountmicroservice.repository.model.AccountJpa;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockMvcClientHttpRequestFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Slf4j
public class AccountsIntegrationTest {
    @LocalServerPort
    private int port;

    private String baseUrl = "http://localhost";

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AccountsRepository accountsRepository;



    @BeforeEach
    void setUp() {
        this.baseUrl = this.baseUrl + ":" +port+ "/accounts";

    }
    // Add account Success
    @Test
    public void testAddAccount_Then_201(){
        Account newAccount = new Account("mario.rossi888@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        newAccount.setName("Mario");
        newAccount.setSurname("Rossi");

        ResponseEntity<Account> response = this.testRestTemplate.postForEntity(this.baseUrl, newAccount, Account.class);

        Account savedAccount = response.getBody();

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(savedAccount);
        assertEquals(newAccount.getEmail(),savedAccount.getEmail());
        assertEquals(newAccount.getName(),savedAccount.getName());
        assertEquals(newAccount.getSurname(),savedAccount.getSurname());
        assertEquals(newAccount.getBirthdate(),savedAccount.getBirthdate());
        assertEquals(newAccount.getGender(),savedAccount.getGender());
    }

    @Test
    void testAddAccountMissing_Name_Surname_Then_201(){
        Account newAccount = new Account("mario.rossi29@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        ResponseEntity<Account> response = this.testRestTemplate.postForEntity(this.baseUrl, newAccount, Account.class);
        Account savedAccount = response.getBody();
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(savedAccount);
        assertEquals(newAccount.getEmail(),savedAccount.getEmail());
        assertNull(newAccount.getName(),savedAccount.getName());
        assertNull(newAccount.getSurname(),savedAccount.getSurname());
        assertEquals(newAccount.getBirthdate(),savedAccount.getBirthdate());
        assertEquals(newAccount.getGender(),savedAccount.getGender());
    }

    // Add Account Failed

    @Test
    void testAddAccount_AlreadyCreated_Then_409() throws Exception {
        // First, create the account
        Account newAccount0 = new Account("mario.rossi@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        newAccount0.setName("Mario");
        newAccount0.setSurname("Rossi");

        this.testRestTemplate.postForEntity(this.baseUrl, newAccount0, Account.class);

        // Then try to add an account with the same email of the previous one
        Account newAccount = new Account("mario.rossi@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");

        String error = "Conflict! Account with email "+newAccount.getEmail()+" already created!";

        ResponseEntity<String> response = this.testRestTemplate.postForEntity(this.baseUrl, newAccount, String.class);

        JsonNode node = this.objectMapper.readTree(response.getBody());

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(error ,node.get("error").asText());
        log.info("Error -> "+node.get("error").asText());
    }

    @Test
    void testAddAccount_MissingRequiredFields_Then_400() throws  Exception{
        Account newAccount = new Account(null,
                null,
                null,
                null);
        newAccount.setName("Mario");
        newAccount.setSurname("Rossi");

        List<String> errors = new ArrayList<>();
        errors.add("email must not be null");
        errors.add("birthdate must not be null");
        errors.add("gender must not be null");
        errors.add("password must not be null");

        ResponseEntity<String> response = this.testRestTemplate.postForEntity(this.baseUrl, newAccount, String.class);

        JsonNode node = this.objectMapper.readTree(response.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errors.size() ,node.get("error").size());
        for (JsonNode objNode : node.get("error")) {
            assertTrue(errors.contains(objNode.asText()));
            log.info("Error -> "+objNode.asText());
        }

    }

    @Test
    void testAddAccount_FieldSize_Then_400() throws Exception{
        Account newAccount = new Account("m@",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "ds3!");
        newAccount.setName("M");
        newAccount.setSurname("R");

        List<String> errors = new ArrayList<>();
        errors.add("email must be a well-formed email address");
        errors.add("email size must be between 3 and 320");
        errors.add("password must match \"^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$\"");
        errors.add("password size must be between 8 and 255");
        errors.add("name size must be between 2 and 50");
        errors.add("surname size must be between 2 and 50");

        ResponseEntity<String> response = this.testRestTemplate.postForEntity(this.baseUrl, newAccount, String.class);

        JsonNode node = this.objectMapper.readTree(response.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errors.size() ,node.get("error").size());

        for (JsonNode objNode : node.get("error")) {
            assertTrue(errors.contains(objNode.asText()));
            log.info("Error -> "+objNode.asText());
        }
    }

    // Add Account Failed BIRTHDATE

    @Test
    void testAddAccount_Birthdate_TypeError_Then_400() throws Exception{
        Account newAccount = new Account("mario.rossi@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        newAccount.setName("Mario");
        newAccount.setSurname("Rossi");

        JsonNode jsonNode = this.objectMapper.readTree(this.objectMapper.writeValueAsString(newAccount));
        ((ObjectNode) jsonNode).put("birthdate","202308-05");
        String newAccountJson = this.objectMapper.writeValueAsString(jsonNode);

        String error = "JSON parse error: Cannot deserialize value of type `java.time.LocalDate` from String \"202308-05\": Failed to deserialize java.time.LocalDate: (java.time.format.DateTimeParseException) Text '202308-05' could not be parsed at index 0";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<String>(newAccountJson, headers);

        ResponseEntity<String> response = this.testRestTemplate.postForEntity(this.baseUrl, request, String.class);

        JsonNode node = this.objectMapper.readTree(response.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(error, node.get("error").asText());
        log.info("Error -> "+node.get("error").asText());
    }

    @Test
    void testAddAccount_Birthdate_UnderAge_Then_400() throws Exception{
        Account newAccount = new Account("mario.rossi7@gmail.com",
                LocalDate.of(2010,2,2),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        newAccount.setName("Mario");
        newAccount.setSurname("Rossi");

        String error = "birthdate is not valid! The user must be an adult";

        ResponseEntity<String> response = this.testRestTemplate.postForEntity(this.baseUrl, newAccount, String.class);
        JsonNode node = this.objectMapper.readTree(response.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(node.get("error").isArray());
        assertEquals(error, node.get("error").get(0).asText());
        log.info("Error -> "+node.get("error").get(0));
    }

    @Test
    void testAddAccount_Gender_TypeError_Then_400() throws Exception{
        Account newAccount = new Account("mario.rossi@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        newAccount.setName("Mario");
        newAccount.setSurname("Rossi");

        JsonNode jsonNode = this.objectMapper.readTree(this.objectMapper.writeValueAsString(newAccount));
        ((ObjectNode) jsonNode).put("gender","male");
        String newAccountJson = this.objectMapper.writeValueAsString(jsonNode);

        String error = "JSON parse error: Cannot construct instance of `com.sergiostefanizzi.accountmicroservice.model.Account$GenderEnum`, problem: Unexpected value 'male'";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<String>(newAccountJson, headers);

        ResponseEntity<String> response = this.testRestTemplate.postForEntity(this.baseUrl, request, String.class);

        JsonNode node = this.objectMapper.readTree(response.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(error, node.get("error").asText());
        log.info("Error -> "+node.get("error").asText());
    }
    // Delete Account Success
    @Test
    void testDeleteAccountById_Then_204() throws Exception{
        Account newAccount = new Account("mario.rossi2@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        newAccount.setName("Mario");
        newAccount.setSurname("Rossi");

        ResponseEntity<Account> responsePost = this.testRestTemplate.postForEntity(this.baseUrl, newAccount, Account.class);

        Account savedAccount = responsePost.getBody();
        assertNotNull(savedAccount);
        ResponseEntity<Void> responseDelete = this.testRestTemplate.exchange(this.baseUrl+"/{accountId}", HttpMethod.DELETE, HttpEntity.EMPTY, Void.class,savedAccount.getId());
        assertEquals(HttpStatus.NO_CONTENT, responseDelete.getStatusCode());
        Optional<AccountJpa> deletedAccountJpa = this.accountsRepository.findById(savedAccount.getId());
        assertTrue(deletedAccountJpa.isPresent());
        assertNotNull(deletedAccountJpa.get().getDeletedAt());
        log.info("DeletedAt --> "+deletedAccountJpa.get().getDeletedAt());
    }

    // Delete Account Failed
    @Test
    void testDeleteAccountById_Then_404() throws Exception{
        ResponseEntity<String> response = this.testRestTemplate.exchange(this.baseUrl+"/9999", HttpMethod.DELETE, HttpEntity.EMPTY, String.class);
        String error = "Account with id 9999 not found!";
        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(error, node.get("error").asText());
        log.info("Error --> "+node.get("error").asText());
    }

    @Test
    void testDeleteAccountById_Then_400() throws Exception{
        ResponseEntity<String> response = this.testRestTemplate.exchange(this.baseUrl+"/rgfd", HttpMethod.DELETE, HttpEntity.EMPTY, String.class);
        String error = "Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'; For input string: \"rgfd\"";
        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(error, node.get("error").asText());
        log.info("Error --> "+node.get("error").asText());
    }

    // Update Account SUCCESS
    /*
    @Test
    void testUpdateAccountBy_Then_200() throws Exception{
        Account newAccount = new Account("mario.rossi3@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        newAccount.setName("Mario");
        newAccount.setSurname("Rossi");

        ResponseEntity<Account> responsePost = this.testRestTemplate.postForEntity(this.baseUrl, newAccount, Account.class);

        Account savedAccount = responsePost.getBody();
        assertNotNull(savedAccount);
        assertEquals(HttpStatus.CREATED, responsePost.getStatusCode());
        log.info("savedAccount --> "+savedAccount.getEmail());

        AccountPatch accountPatchToUpdate = new AccountPatch();
        accountPatchToUpdate.setName("Marietta");
        accountPatchToUpdate.setSurname("Verdi");
        accountPatchToUpdate.setGender(AccountPatch.GenderEnum.FEMALE);
        accountPatchToUpdate.setPassword("hhh3h!d2h234h4");


        String accountPatchToUpdateJSON = this.objectMapper.writeValueAsString(accountPatchToUpdate);

        this.mockMvc.perform(patch("/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(accountPatchToUpdateJSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedAccount.getId()))
                .andExpect(jsonPath("$.email").value(savedAccount.getEmail()))
                .andExpect(jsonPath("$.name").value(accountPatchToUpdate.getName()))
                .andExpect(jsonPath("$.surname").value(accountPatchToUpdate.getSurname()))
                .andExpect(jsonPath("$.birthdate").value(savedAccount.getBirthdate().toString()))
                .andExpect(jsonPath("$.gender").value(accountPatchToUpdate.getGender().toString()));

    }

     */
}
