package com.sergiostefanizzi.accountmicroservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.service.AdminsService;
import com.sergiostefanizzi.accountmicroservice.service.KeycloakService;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AccountNotFoundException;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AdminAlreadyCreatedException;
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
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasSize;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest(AdminsController.class)
//blocca spring security nei test
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Slf4j
class AdminsControllerTest {
    @MockBean
    private AdminsService adminsService;
    @MockBean
    private KeycloakService keycloakService;
    @MockBean
    private SecurityContext securityContext;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private Account savedAccount;
    private Account savedAccount1;
    private Account savedAccount2;
    private Account savedAccount3;
    private JwtAuthenticationToken jwtAuthenticationToken;
    private final String accountId = UUID.randomUUID().toString();
    private final String accountId1 = UUID.randomUUID().toString();
    private final String accountId2 = UUID.randomUUID().toString();
    private final String accountId3 = UUID.randomUUID().toString();
    private final String invalidId = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {

        this.savedAccount = new Account("pinco.pallino@gmail.com",
                LocalDate.of(1990,4,4),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        this.savedAccount.setName("Pinco");
        this.savedAccount.setSurname("Pallino");
        this.savedAccount.setId(this.accountId);

        SecurityContextHolder.setContext(securityContext);

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


        this.savedAccount1 = new Account("giuseppe.verdi@gmail.com",
                LocalDate.of(1970,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        this.savedAccount1.setName("Giuseppe");
        this.savedAccount1.setSurname("Verdi");
        this.savedAccount1.setId(accountId1);

        this.savedAccount2 = new Account("mario_bros@live.it",
                LocalDate.of(1995,2,1),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        this.savedAccount2.setName("Mario");
        this.savedAccount2.setSurname("Bros");
        this.savedAccount2.setId(accountId2);

        this.savedAccount3 = new Account("luigi_bro@outlook.com",
                LocalDate.of(1995,2,1),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        this.savedAccount3.setName("Luigi");
        this.savedAccount3.setSurname("Bros");
        this.savedAccount3.setId(accountId3);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testAddAdminById_Then_201() throws Exception{

        when(this.keycloakService.checkActiveById(anyString())).thenReturn(true);

        when(this.securityContext.getAuthentication()).thenReturn(this.jwtAuthenticationToken);

        when(this.adminsService.save(anyString())).thenReturn(this.accountId);

        MvcResult result = this.mockMvc.perform(put("/admins/{accountId}",this.accountId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").value(this.accountId))
                .andReturn();

        // salvo risposta in result per visualizzarla
        String resultAsString = result.getResponse().getContentAsString();
        log.info("New Admin ID --> "+resultAsString);
    }


    @Test
    void testAddAdminById_Then_404() throws Exception{
        when(this.securityContext.getAuthentication()).thenReturn(this.jwtAuthenticationToken);
        when(this.keycloakService.checkActiveById(anyString())).thenReturn(false);

        MvcResult result = this.mockMvc.perform(put("/admins/{accountId}",accountId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(res-> assertTrue(res.getResolvedException() instanceof AccountNotFoundException))
                .andExpect(jsonPath("$.error").value("Account with id "+accountId+" not found!"))
                .andReturn();

        String resultAsString = result.getResponse().getContentAsString();
        log.info("Errors\n"+resultAsString);

    }

    @Test
    void testAddAdminById_Then_409() throws Exception{
        when(this.keycloakService.checkActiveById(anyString())).thenReturn(true);
        when(this.securityContext.getAuthentication()).thenReturn(this.jwtAuthenticationToken);

        when(this.adminsService.save(anyString())).thenThrow(new AdminAlreadyCreatedException(accountId));

        MvcResult result = this.mockMvc.perform(put("/admins/{accountId}",accountId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(res-> assertTrue(res.getResolvedException() instanceof AdminAlreadyCreatedException))
                .andExpect(jsonPath("$.error").value("Conflict! Admin with id "+accountId+" already created!"))
                .andReturn();

        String resultAsString = result.getResponse().getContentAsString();
        log.info("Errors\n"+resultAsString);
    }

    @Test
    void deleteAdminById_Then_204() throws Exception{
        when(this.keycloakService.checkActiveById(anyString())).thenReturn(true);
        when(this.securityContext.getAuthentication()).thenReturn(this.jwtAuthenticationToken);

        this.mockMvc.perform(delete("/admins/{accountId}",accountId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

    }

    @Test
    void deleteAdminById_Then_404() throws Exception{
        when(this.securityContext.getAuthentication()).thenReturn(this.jwtAuthenticationToken);
        when(this.keycloakService.checkActiveById(anyString())).thenReturn(false);
        MvcResult result = this.mockMvc.perform(delete("/admins/{accountId}",accountId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(res-> assertTrue(res.getResolvedException() instanceof AccountNotFoundException))
                .andExpect(jsonPath("$.error").value("Account with id "+accountId+" not found!"))
                .andReturn();

        String resultAsString = result.getResponse().getContentAsString();
        log.info("Errors\n"+resultAsString);
    }

    //FIND ALL ACCOUNTS
    @Test
    void testFindAllAccounts_Then_200() throws Exception{
        List<Account> activeAccountList = asList(
                this.savedAccount1,
                this.savedAccount2,
                this.savedAccount3
        );

        when(this.adminsService.findAll(anyBoolean())).thenReturn(activeAccountList);

        MvcResult result = this.mockMvc.perform(get("/admins/accounts?removedAccount={removedAccount}",false)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*").isArray())
                .andExpect(jsonPath("$.*", hasSize(3)))
                .andExpect(jsonPath("$.[0]id").value(this.savedAccount1.getId()))
                .andExpect(jsonPath("$.[0]name").value(this.savedAccount1.getName()))
                .andExpect(jsonPath("$.[0]surname").value(this.savedAccount1.getSurname()))
                .andExpect(jsonPath("$.[0]email").value(this.savedAccount1.getEmail()))
                .andExpect(jsonPath("$.[0]birthdate").value(this.savedAccount1.getBirthdate().toString()))
                .andExpect(jsonPath("$.[0]gender").value(this.savedAccount1.getGender().toString()))
                .andExpect(jsonPath("$.[1]id").value(this.savedAccount2.getId()))
                .andExpect(jsonPath("$.[1]name").value(this.savedAccount2.getName()))
                .andExpect(jsonPath("$.[1]surname").value(this.savedAccount2.getSurname()))
                .andExpect(jsonPath("$.[1]email").value(this.savedAccount2.getEmail()))
                .andExpect(jsonPath("$.[1]birthdate").value(this.savedAccount2.getBirthdate().toString()))
                .andExpect(jsonPath("$.[1]gender").value(this.savedAccount2.getGender().toString()))
                .andExpect(jsonPath("$.[2]id").value(this.savedAccount3.getId()))
                .andExpect(jsonPath("$.[2]name").value(this.savedAccount3.getName()))
                .andExpect(jsonPath("$.[2]surname").value(this.savedAccount3.getSurname()))
                .andExpect(jsonPath("$.[2]email").value(this.savedAccount3.getEmail()))
                .andExpect(jsonPath("$.[2]birthdate").value(this.savedAccount3.getBirthdate().toString()))
                .andExpect(jsonPath("$.[2]gender").value(this.savedAccount3.getGender().toString()))
                .andReturn();

        log.info(result.getResponse().getContentAsString());
    }

    @Test
    void testFindAllAccounts_Then_400() throws Exception{

        MvcResult result = this.mockMvc.perform(get("/admins/accounts")
                        .queryParam("removedAccount", "NotBoolean")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(res-> assertTrue(res.getResolvedException() instanceof MethodArgumentTypeMismatchException))
                .andExpect(jsonPath("$.error").value("Type mismatch"))
                .andReturn();

        String resultAsString = result.getResponse().getContentAsString();
        log.info("Errors\n"+resultAsString);
    }

    @Test
    void testFindAllAccounts_MissingQueryParam_Then_400() throws Exception{

        MvcResult result = this.mockMvc.perform(get("/admins/accounts")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(res-> assertTrue(res.getResolvedException() instanceof MissingServletRequestParameterException))
                .andExpect(jsonPath("$.error").value("Required request parameter is missing"))
                .andReturn();

        String resultAsString = result.getResponse().getContentAsString();
        log.info("Errors\n"+resultAsString);
    }









}