package com.sergiostefanizzi.accountmicroservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.Admin;
import com.sergiostefanizzi.accountmicroservice.repository.AccountsRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Slf4j
public class AdminsIntegrationTest {
    @LocalServerPort
    private int port;
    private String baseUrl = "http://localhost";
    private String baseUrlAccounts;
    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private Account newAccount;


    @BeforeEach
    void setUp() {
        this.baseUrlAccounts = this.baseUrl + ":" +port+ "/accounts";
        this.baseUrl = this.baseUrl + ":" +port+ "/admins";
    }

    @Test
    void testAddAdminById_Then_201() throws Exception{

        // account creation
        Account newAccount = new Account("giuseppe.verdi@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        newAccount.setName("Giuseppe");
        newAccount.setSurname("Verdi");

        ResponseEntity<Account> responsePost = this.testRestTemplate.postForEntity(this.baseUrlAccounts, newAccount, Account.class);

        Account savedAccount = responsePost.getBody();
        assertEquals(HttpStatus.CREATED, responsePost.getStatusCode());
        assertNotNull(savedAccount);

        // set admin flag to true
        ResponseEntity<Admin> response = this.testRestTemplate.exchange(this.baseUrl+"/{accountId}",
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                Admin.class,
                savedAccount.getId());
        Admin savedAdmin = response.getBody();
        assertNotNull(savedAdmin);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(savedAccount.getId(), savedAdmin.getId());
        log.info("Saved Account ID --> "+savedAccount.getId());
        log.info("Saved Admin ID --> "+savedAdmin.getId());

    }

    @Test
    void testAddAdminById_Then_400() throws Exception{
        ResponseEntity<String> response = this.testRestTemplate.exchange(this.baseUrl+"/notLong",
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                String.class);

        String error = "Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'; For input string: \"notLong\"";

        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(error ,node.get("error").asText());
        log.info("Error -> "+node.get("error").asText());
    }

    @Test
    void testAddAdminById_Then_404() throws Exception{
        ResponseEntity<String> response = this.testRestTemplate.exchange(this.baseUrl+"/123456",
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                String.class);

        String error = "Account with id 123456 not found!";

        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(error ,node.get("error").asText());
        log.info("Error -> "+node.get("error").asText());
    }

    @Test
    void testAddAdminById_Then_409() throws Exception{
        // account creation
        Account newAccount = new Account("giuseppe.verdi.conflict@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        newAccount.setName("Giuseppe");
        newAccount.setSurname("Verdi");

        ResponseEntity<Account> responsePost = this.testRestTemplate.postForEntity(this.baseUrlAccounts, newAccount, Account.class);

        Account savedAccount = responsePost.getBody();
        assertEquals(HttpStatus.CREATED, responsePost.getStatusCode());
        assertNotNull(savedAccount);

        // set admin flag to true
        ResponseEntity<Admin> responsePut = this.testRestTemplate.exchange(this.baseUrl+"/{accountId}",
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                Admin.class,
                savedAccount.getId());
        Admin savedAdmin = responsePut.getBody();
        assertNotNull(savedAdmin);
        assertEquals(HttpStatus.CREATED, responsePut.getStatusCode());
        assertEquals(savedAccount.getId(), savedAdmin.getId());
        log.info("Saved Account ID --> "+savedAccount.getId());
        log.info("Saved Admin ID --> "+savedAdmin.getId());

        // set admin flag to true another time
        ResponseEntity<String> response = this.testRestTemplate.exchange(this.baseUrl+"/{accountId}",
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                String.class,
                savedAccount.getId());

        String error = "Conflict! Admin with id "+savedAccount.getId()+" already created!";

        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(error ,node.get("error").asText());
        log.info("Error -> "+node.get("error").asText());
    }

    @Test
    void deleteAdminById_Then_204() throws Exception{
        // account creation
        Account newAccount = new Account("giuseppe.verdi.delete@gmail.com",
                LocalDate.of(1990,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        newAccount.setName("Giuseppe");
        newAccount.setSurname("Verdi");

        ResponseEntity<Account> responsePost = this.testRestTemplate.postForEntity(this.baseUrlAccounts, newAccount, Account.class);

        Account savedAccount = responsePost.getBody();
        assertEquals(HttpStatus.CREATED, responsePost.getStatusCode());
        assertNotNull(savedAccount);

        // delete account
        ResponseEntity<Void> response = this.testRestTemplate.exchange(this.baseUrl+"/{accountId}", HttpMethod.DELETE, HttpEntity.EMPTY, Void.class, savedAccount.getId());
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void deleteAdminById_Then_400() throws Exception{
        ResponseEntity<String> response = this.testRestTemplate.exchange(this.baseUrl+"/notLong",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                String.class);

        String error = "Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'; For input string: \"notLong\"";

        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(error ,node.get("error").asText());
        log.info("Error -> "+node.get("error").asText());
    }

    @Test
    void deleteAdminById_Then_404() throws Exception{
        ResponseEntity<String> response = this.testRestTemplate.exchange(this.baseUrl+"/123456",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                String.class);

        String error = "Account with id 123456 not found!";

        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(error ,node.get("error").asText());
        log.info("Error -> "+node.get("error").asText());
    }

    @Test
    void testFindAllAccounts_Then_200() throws Exception{
        addAccountsList();
        ResponseEntity<String> response = this.testRestTemplate.exchange(this.baseUrl+"/accounts?removedAccount=false",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertTrue(node.isArray());
        assertTrue(node.size() >= 0);
        for (JsonNode jsonNode : node){
            log.info("Account --> "+jsonNode);
        }
    }

    @Test
    void testFindAllAccounts_Then_400() throws Exception{
        ResponseEntity<String> response = this.testRestTemplate.exchange(this.baseUrl+"/accounts?removedAccount=notBool",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                String.class);

        String error = "Failed to convert value of type 'java.lang.String' to required type 'java.lang.Boolean'; Invalid boolean value [notBool]";
        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(error ,node.get("error").asText());
        log.info("Error -> "+node.get("error").asText());
    }

    void addAccountsList(){
        List<Account> accountList = new ArrayList<>();
        Account account1 = new Account(
                "acc1@gmail.com",
                LocalDate.of(1990,3,4),
                Account.GenderEnum.MALE,
                "Cdifdf23!!"
        );

        Account account2 = new Account(
                "acc2@gmail.com",
                LocalDate.of(1995,4,14),
                Account.GenderEnum.FEMALE,
                "XXfdsdf23!4!"
        );
        Account account3 = new Account(
                "acc3@gmail.com",
                LocalDate.of(2000,5,5),
                Account.GenderEnum.MALE,
                "Cdirtwqwqqqf2563!"
        );

        ResponseEntity<Account> responsePost1 = this.testRestTemplate.postForEntity(this.baseUrlAccounts, account1, Account.class);
        ResponseEntity<Account> responsePost2 = this.testRestTemplate.postForEntity(this.baseUrlAccounts, account2, Account.class);
        ResponseEntity<Account> responsePost3 = this.testRestTemplate.postForEntity(this.baseUrlAccounts, account3, Account.class);
        log.info(String.valueOf("STATO1 -->"+responsePost1.getStatusCode()));
        log.info(String.valueOf("STATO2 -->"+responsePost2.getStatusCode()));
        log.info(String.valueOf("STATO3 -->"+responsePost3.getStatusCode()));
    }
}
