package com.sergiostefanizzi.accountmicroservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.repository.AccountsRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Slf4j
public class AdminsIT {
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
    private Account savedAccount1;
    private Account savedAccount2;
    private Account savedAccount3;
    private String invalidId = UUID.randomUUID().toString();



    @BeforeEach
    void setUp() {
        //this.baseUrlAccounts = this.baseUrl + ":" +port+ "/accounts";
        this.baseUrl = this.baseUrl + ":" +port+ "/admins";

        this.savedAccount1 = new Account("pinco.pallino@gmail.com",
                LocalDate.of(1970,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        this.savedAccount1.setName("Pinco");
        this.savedAccount1.setSurname("Pallino");
        this.savedAccount1.setId("c8318aba-2312-46bf-a9a9-872102df1ee5");


        this.savedAccount2 = new Account("pinco.pallino2@gmail.com",
                LocalDate.of(1970,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        this.savedAccount2.setName("Pinco");
        this.savedAccount2.setSurname("Pallino");
        this.savedAccount2.setId("da9bab94-e963-4803-b262-50b925ed7540");

        this.savedAccount3 = new Account("pinco.pallino3@gmail.com",
                LocalDate.of(1970,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        this.savedAccount3.setName("Pinco");
        this.savedAccount3.setSurname("Pallino");
        this.savedAccount3.setId("28ac0564-8f56-44d6-ada5-1050753cfbbd");
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

    @Test
    void testAddAdminById_Then_201() throws Exception{
        String accessToken = getAccessToken(this.savedAccount1);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<String> response = this.testRestTemplate.exchange(this.baseUrl+"/{accountId}",
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                String.class,
                this.savedAccount1.getId());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        String savedAdminId = response.getBody();
        assertNotNull(savedAdminId);
        assertEquals(this.savedAccount1.getId(), savedAdminId);
        log.info("New Admin with Id --> "+savedAdminId);

    }
/*
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
