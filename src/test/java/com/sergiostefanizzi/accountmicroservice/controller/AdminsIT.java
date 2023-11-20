package com.sergiostefanizzi.accountmicroservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Collections;
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
    private Account savedAdmin;
    //admin account
    private Account savedAdminToBe;
    private Account savedAccountLoginError;
    private Account savedAdminToDelete;
    private final String invalidId = UUID.randomUUID().toString();



    @BeforeEach
    void setUp() {
        //this.baseUrlAccounts = this.baseUrl + ":" +port+ "/accounts";
        this.baseUrl = this.baseUrl + ":" +port+ "/admins";

        this.savedAdmin = new Account("pinco.admin@gmail.com",
                LocalDate.of(1970,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        this.savedAdmin.setName("Pinco");
        this.savedAdmin.setSurname("Pallino");
        this.savedAdmin.setId("66d8ca7e-cc64-48a6-a9f6-d611055ba24a");


        this.savedAdminToBe = new Account("pinco.admin.to.be@gmail.com",
                LocalDate.of(1970,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        this.savedAdminToBe.setName("Pinco");
        this.savedAdminToBe.setSurname("Pallino");
        this.savedAdminToBe.setId("14e80daf-ccbd-4a7f-ac83-1cacf809fc07");


        this.savedAdminToDelete = new Account("pinco.admin.delete@gmail.com",
                LocalDate.of(1970,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        this.savedAdminToDelete.setName("Pinco");
        this.savedAdminToDelete.setSurname("Pallino");
        this.savedAdminToDelete.setId("6168d0b9-6aa1-43e8-9fd0-ce1c57f95448");

        this.savedAccountLoginError = new Account("pinco.login.error@gmail.com",
                LocalDate.of(1970,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        this.savedAccountLoginError.setName("Pinco");
        this.savedAccountLoginError.setSurname("Pallino");
        this.savedAccountLoginError.setId("5c62d672-cfa5-4ebf-a9c3-1622198c69b");


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

    private void restoreDeletedAdmin(Account account) {
        RealmResource realmResource = this.keycloak.realm("social-accounts");
        UsersResource usersResource = realmResource
                .users();
        UserResource userResource = usersResource
                .get(account.getId());
        UserRepresentation userRepresentation = userResource
                .toRepresentation();

        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(true);

        userResource
                .update(userRepresentation);


        RoleRepresentation userRealmRole = realmResource
                .roles()
                .get("admin")
                .toRepresentation();

        RoleMappingResource roles = usersResource
                .get(account.getId())
                .roles();

        RoleScopeResource roleScopeResource = roles
                .realmLevel();
        roleScopeResource.add(Collections.singletonList(userRealmRole));
    }

    private void removeAdminRole(Account account) {
        RealmResource realmResource = this.keycloak.realm("social-accounts");
        RoleRepresentation userRealmRole = realmResource
                .roles()
                .get("admin")
                .toRepresentation();

        RoleMappingResource roles = realmResource
                .users()
                .get(account.getId())
                .roles();

        RoleScopeResource roleScopeResource = roles
                .realmLevel();
        roleScopeResource.remove(Collections.singletonList(userRealmRole));
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
        String accessToken = getAccessToken(this.savedAdmin);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<String> response = this.testRestTemplate.exchange(this.baseUrl+"/{accountId}",
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                String.class,
                this.savedAdminToBe.getId());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        String savedAdminId = response.getBody();
        assertNotNull(savedAdminId);
        assertEquals(this.savedAdminToBe.getId(), savedAdminId);
        log.info("New Admin with Id --> "+savedAdminId);

        removeAdminRole(this.savedAdminToBe);
    }

    @Test
    void testAddAdminById_Then_401(){
        ResponseEntity<String> response = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}",
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                String.class,
                this.savedAccountLoginError.getId());

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testAddAdminById_Then_403() throws Exception{
        String accessToken = getAccessToken(this.savedAccountLoginError);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<String> response = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}",
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                String.class,
                this.savedAdminToBe.getId());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testAddAdminById_EmailNotValidated_Then_403() throws Exception{
        String accessToken = getAccessToken(this.savedAdmin);
        invalidateEmail(this.savedAdminToBe);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<String> response = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}",
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                String.class,
                this.savedAdminToBe.getId());

        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Account's email with id "+this.savedAdminToBe.getId()+" is not validated", node.get("error").asText());
        log.info("Error --> "+node.get("error").asText());
        validateEmail(this.savedAdminToBe);
    }

    @Test
    void testAddAdminById_AlreadyCreated_Then_403() throws Exception{
        String accessToken = getAccessToken(this.savedAdmin);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<String> response = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}",
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                String.class,
                this.savedAdmin.getId());

        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Forbidden: Account with id "+this.savedAdmin.getId()+" can not perform this action", node.get("error").asText());
        log.info("Error --> " + node.get("error").asText());
    }

    @Test
    void testAddAdminById_Then_404() throws Exception{
        String accessToken = getAccessToken(this.savedAdmin);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<String> response = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}",
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                String.class,
                this.invalidId);

        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Account with id "+this.invalidId+" not found!", node.get("error").asText());
        log.info("Error --> " + node.get("error").asText());
    }



    @Test
    void deleteAdminById_DeleteAdmin_Then_204() throws Exception{
        String accessToken = getAccessToken(this.savedAdmin);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<Void> response = this.testRestTemplate.exchange(this.baseUrl+"/{accountId}",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class,
                this.savedAdminToDelete.getId());
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        restoreDeletedAdmin(this.savedAdminToDelete);
    }

    @Test
    void deleteAdminById_DeleteUser_Then_204() throws Exception{
        String accessToken = getAccessToken(this.savedAdmin);
        removeAdminRole(this.savedAdminToDelete);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<Void> response = this.testRestTemplate.exchange(this.baseUrl+"/{accountId}",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class,
                this.savedAdminToDelete.getId());
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        restoreDeletedAdmin(this.savedAdminToDelete);
    }

    @Test
    void deleteAdminById_Then_401(){
        ResponseEntity<String> response = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                String.class,
                this.savedAccountLoginError.getId());

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void deleteAdminById_Then_403() throws Exception{
        String accessToken = getAccessToken(this.savedAccountLoginError);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<String> response = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                String.class,
                this.savedAdminToDelete.getId());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void deleteAdminById_Then_404() throws Exception{
        String accessToken = getAccessToken(this.savedAdmin);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<String> response = this.testRestTemplate.exchange(
                this.baseUrl+"/{accountId}",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                String.class,
                invalidId);

        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Account with id "+invalidId+" not found!", node.get("error").asText());
        log.info("Error --> " + node.get("error").asText());
    }

    @Test
    void testFindAllAccounts_Then_200() throws Exception{
        String accessToken = getAccessToken(this.savedAdmin);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<List<Account>> responseAccountList = this.testRestTemplate.exchange(
                this.baseUrl+"/accounts?removedAccount={removedProfile}",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<Account>>() {
                },
                false);
        assertEquals(HttpStatus.OK, responseAccountList.getStatusCode());
        assertNotNull(responseAccountList.getBody());
        List<Account> savedProfileList = responseAccountList.getBody();
        assertTrue(savedProfileList.size() >= 6);
        log.info(responseAccountList.toString());
    }

    @Test
    void testFindAllAccounts_Deleted_Then_200() throws Exception{
        deletedUser(this.savedAdminToDelete);
        String accessToken = getAccessToken(this.savedAdmin);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<List<Account>> responseAccountList = this.testRestTemplate.exchange(
                this.baseUrl+"/accounts?removedAccount={removedProfile}",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<Account>>() {
                },
                true);
        assertEquals(HttpStatus.OK, responseAccountList.getStatusCode());
        assertNotNull(responseAccountList.getBody());
        List<Account> savedProfileList = responseAccountList.getBody();
        assertTrue(savedProfileList.size() >= 5);
        log.info(responseAccountList.toString());
        restoreDeletedUser(this.savedAdminToDelete);
    }

    @Test
    void testFindAllAccounts_Then_400() throws Exception{
        String accessToken = getAccessToken(this.savedAdmin);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<String> response = this.testRestTemplate.exchange(this.baseUrl+"/accounts?removedAccount=notBoolean",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        String error = "Type mismatch";
        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(error ,node.get("error").asText());
        log.info("Error -> "+node.get("error").asText());
    }

    @Test
    void testFindAllAccounts_MissingQueryParam_Then_400() throws Exception{
        String accessToken = getAccessToken(this.savedAdmin);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<String> response = this.testRestTemplate.exchange(this.baseUrl+"/accounts",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        String error = "Required request parameter is missing";
        JsonNode node = this.objectMapper.readTree(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(error ,node.get("error").asText());
        log.info("Error -> "+node.get("error").asText());
    }

    @Test
    void testFindAllAccounts_Then_401(){
        ResponseEntity<String> response = this.testRestTemplate.exchange(this.baseUrl+"/accounts",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
    @Test
    void testFindAllAccounts_Then_403() throws Exception{
        String accessToken = getAccessToken(this.savedAccountLoginError);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<String> response = this.testRestTemplate.exchange(this.baseUrl+"/accounts",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }





}
