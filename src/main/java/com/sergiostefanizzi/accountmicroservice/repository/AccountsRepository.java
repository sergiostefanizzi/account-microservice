package com.sergiostefanizzi.accountmicroservice.repository;

import com.sergiostefanizzi.accountmicroservice.repository.model.AccountJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountsRepository extends JpaRepository<AccountJpa, Long> {
}
