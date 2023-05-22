package com.sergiostefanizzi.accountmicroservice.service;

import com.sergiostefanizzi.accountmicroservice.controller.converter.AccountsToJpaConverter;
import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.repository.AccountsRepository;
import com.sergiostefanizzi.accountmicroservice.repository.AdminsRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AdminsService {
    @Autowired
    private AdminsRepository adminsRepository;
    @Autowired
    private AccountsRepository accountsRepository;
    @Autowired
    private AccountsToJpaConverter accountsToJpaConverter;

    @Transactional
    public List<Account> findAll(Boolean removedAccount) {

        return this.accountsRepository.findAll()
                .stream()
                .filter(accountJpa ->
                        (accountJpa.getDeletedAt() == null && !removedAccount)
                || (accountJpa.getDeletedAt() != null && removedAccount))
                .map(this.accountsToJpaConverter::convertBack)
                .collect(Collectors.toList());
    }
}
