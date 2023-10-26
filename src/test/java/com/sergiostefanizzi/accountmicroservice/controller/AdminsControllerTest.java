package com.sergiostefanizzi.accountmicroservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiostefanizzi.accountmicroservice.repository.AccountsRepository;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.Admin;
import com.sergiostefanizzi.accountmicroservice.service.AdminsService;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
//@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Slf4j
class AdminsControllerTest {
    @MockBean
    private AdminsService adminsService;
    @MockBean
    private AccountsRepository accountsRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private Account savedAccount1;
    private Account savedAccount2;
    private Account savedAccount3;
    @BeforeEach
    void setUp() {
        this.savedAccount1 = new Account("pinco.pallino@gmail.com",
                LocalDate.of(1970,3,15),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        this.savedAccount1.setName("Pinco");
        this.savedAccount1.setSurname("Pallino");
        this.savedAccount1.setId(101L);

        this.savedAccount2 = new Account("mario_bros@live.it",
                LocalDate.of(1995,2,1),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        this.savedAccount2.setName("Mario");
        this.savedAccount2.setSurname("Bros");
        this.savedAccount2.setId(102L);

        this.savedAccount3 = new Account("luigi_bro@outlook.com",
                LocalDate.of(1995,2,1),
                Account.GenderEnum.MALE,
                "dshjdfkdjsf32!");
        this.savedAccount3.setName("Luigi");
        this.savedAccount3.setSurname("Bros");
        this.savedAccount3.setId(103L);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testAddAdminById_Then_201() throws Exception{
        Long accountId = this.savedAccount1.getId();
        Admin newAdmin = new Admin(accountId);

        when(this.accountsRepository.checkActiveById(anyLong())).thenReturn(Optional.of(accountId));
        when(this.adminsService.save(anyLong())).thenReturn(newAdmin);

        MvcResult result = this.mockMvc.perform(put("/admins/{accountId}",accountId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$.id").value(newAdmin.getId()))
                .andReturn();

        // salvo risposta in result per visualizzarla
        String resultAsString = result.getResponse().getContentAsString();
        Admin adminResult = this.objectMapper.readValue(resultAsString, Admin.class);

        log.info(adminResult.toString());
    }

    @Test
    void testAddAdminById_Then_400() throws Exception{
        MvcResult result = this.mockMvc.perform(put("/admins/{accountId}","IdNotLong")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(res-> assertTrue(res.getResolvedException() instanceof NumberFormatException))
                .andExpect(jsonPath("$.error").value("ID is not valid!"))
                .andReturn();

        String resultAsString = result.getResponse().getContentAsString();
        log.info("Errors\n"+resultAsString);
    }

    @Test
    void testAddAdminById_Then_404() throws Exception{
        Long accountId = this.savedAccount1.getId();

        when(this.accountsRepository.checkActiveById(anyLong())).thenReturn(Optional.empty());

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
        Long accountId = this.savedAccount1.getId();

        when(this.accountsRepository.checkActiveById(anyLong())).thenReturn(Optional.of(accountId));
        when(this.adminsService.save(anyLong())).thenThrow(new AdminAlreadyCreatedException(accountId));

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
        Long accountId = this.savedAccount1.getId();

        when(this.accountsRepository.checkActiveById(anyLong())).thenReturn(Optional.of(accountId));

        this.mockMvc.perform(delete("/admins/{accountId}",accountId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

    }

    @Test
    void deleteAdminById_Then_400() throws Exception{
        MvcResult result = this.mockMvc.perform(delete("/admins/{accountId}","IdNotLong")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(res-> assertTrue(res.getResolvedException() instanceof NumberFormatException))
                .andExpect(jsonPath("$.error").value("ID is not valid!"))
                .andReturn();

        String resultAsString = result.getResponse().getContentAsString();
        log.info("Errors\n"+resultAsString);
    }

    @Test
    void deleteAdminById_Then_404() throws Exception{
        Long accountId = this.savedAccount1.getId();

        when(this.accountsRepository.checkActiveById(anyLong())).thenReturn(Optional.empty());

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
                .andExpect(jsonPath("$.error").value("Failed to convert value of type 'java.lang.String' to required type 'java.lang.Boolean'; Invalid boolean value [NotBoolean]"))
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
                .andExpect(jsonPath("$.error").value("Required request parameter 'removedAccount' for method parameter type Boolean is not present"))
                .andReturn();

        String resultAsString = result.getResponse().getContentAsString();
        log.info("Errors\n"+resultAsString);
    }


}