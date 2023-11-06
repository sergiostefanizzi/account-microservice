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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Slf4j
public class AdminsIT {
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

    private Account savedAccount1;


/*
    @BeforeEach
    void setUp() {
        this.baseUrlAccounts = this.baseUrl + ":" +port+ "/accounts";
        this.baseUrl = this.baseUrl + ":" +port+ "/admins";

        this.savedAccount1 = new Account("pinco.pallino@gmail.com",
                LocalDate.of(1970,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        this.savedAccount1.setName("Pinco");
        this.savedAccount1.setSurname("Pallino");
        this.savedAccount1.setId(101L);
    }

    @Test
    void testAddAdminById_Then_201() throws Exception{
        ResponseEntity<Admin> response = this.testRestTemplate.exchange(this.baseUrl+"/{accountId}",
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                Admin.class,
                this.savedAccount1.getId());
        Admin savedAdmin = response.getBody();
        assertNotNull(savedAdmin);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(this.savedAccount1.getId(), savedAdmin.getId());
        log.info(savedAdmin.toString());

    }

    @Test
    void testAddAdminById_Then_400() throws Exception{
        ResponseEntity<String> response = this.testRestTemplate.exchange(
                this.baseUrl+"/IdNotLong",
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                String.class);

        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("ID is not valid!", node.get("error").asText());
        log.info("Error --> "+node.get("error").asText());
    }

    @Test
    void testAddAdminById_Then_404() throws Exception{
        ResponseEntity<String> response = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}",
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                String.class,
                Long.MAX_VALUE);

        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Account with id "+Long.MAX_VALUE+" not found!", node.get("error").asText());
        log.info("Error --> " + node.get("error").asText());
    }

    @Test
    void testAddAdminById_Then_409() throws Exception{
        ResponseEntity<String> response = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}",
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                String.class,
                109L);

        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Conflict! Admin with id 109 already created!", node.get("error").asText());
        log.info("Error --> " + node.get("error").asText());
    }

    @Test
    void deleteAdminById_Then_204() throws Exception{
        ResponseEntity<Void> response = this.testRestTemplate.exchange(this.baseUrl+"/{accountId}",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                Void.class,
                110L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

    }

    @Test
    void deleteAdminById_Then_400() throws Exception{
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

    @Test
    void deleteAdminById_Then_404() throws Exception{
        ResponseEntity<String> response = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                String.class,
                Long.MAX_VALUE);

        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Account with id "+Long.MAX_VALUE+" not found!", node.get("error").asText());
        log.info("Error --> " + node.get("error").asText());
    }

    @Test
    void testFindAllAccounts_Then_200() throws Exception{
        ResponseEntity<List<Account>> responseAccountList = this.testRestTemplate.exchange(
                this.baseUrl+"/accounts?removedAccount={removedProfile}",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<List<Account>>() {
                },
                false);
        assertEquals(HttpStatus.OK, responseAccountList.getStatusCode());
        assertNotNull(responseAccountList.getBody());
        List<Account> savedProfileList = responseAccountList.getBody();
        assertTrue(savedProfileList.size() >= 8);
        log.info(responseAccountList.toString());
    }

    @Test
    void testFindAllAccounts_Deleted_Then_200() throws Exception{
        ResponseEntity<List<Account>> responseAccountList = this.testRestTemplate.exchange(
                this.baseUrl+"/accounts?removedAccount={removedProfile}",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<List<Account>>() {
                },
                true);
        assertEquals(HttpStatus.OK, responseAccountList.getStatusCode());
        assertNotNull(responseAccountList.getBody());
        List<Account> savedProfileList = responseAccountList.getBody();
        assertTrue(savedProfileList.size() >= 11);
        log.info(responseAccountList.toString());
    }

    @Test
    void testFindAllAccounts_Then_400() throws Exception{

        ResponseEntity<String> response = this.testRestTemplate.exchange(this.baseUrl+"/accounts?removedAccount=notBoolean",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                String.class);

        String error = "Failed to convert value of type 'java.lang.String' to required type 'java.lang.Boolean'; Invalid boolean value [notBoolean]";
        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(error ,node.get("error").asText());
        log.info("Error -> "+node.get("error").asText());
    }

    @Test
    void testFindAllAccounts_MissingQueryParam_Then_400() throws Exception{

        ResponseEntity<String> response = this.testRestTemplate.exchange(this.baseUrl+"/accounts",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                String.class);

        String error = "Required request parameter 'removedAccount' for method parameter type Boolean is not present";
        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(error ,node.get("error").asText());
        log.info("Error -> "+node.get("error").asText());
    }


 */


}
