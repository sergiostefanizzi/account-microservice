package com.sergiostefanizzi.accountmicroservice.repository;

import com.sergiostefanizzi.accountmicroservice.repository.model.AccountJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountsRepository extends JpaRepository<AccountJpa, Long> {
    Optional<AccountJpa> findbyEmail(String email);


}
