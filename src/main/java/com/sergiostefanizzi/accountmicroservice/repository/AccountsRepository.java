package com.sergiostefanizzi.accountmicroservice.repository;

import com.sergiostefanizzi.accountmicroservice.model.AccountJpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountsRepository extends JpaRepository<AccountJpa, Long> {
    //Verifica che l'email passata come parametro è già stata registrata, nel caso ritorna l'intero Account
    //Se c'è un match viene ritornato l'oggetto Optional
    Optional<AccountJpa> findByEmail(String email);

    @Query("SELECT a.id FROM AccountJpa a WHERE a.id=:accountId AND a.deletedAt IS NULL AND a.validatedAt IS NOT NULL")
    Optional<Long> checkActiveById(Long accountId);

    @Query("SELECT a.id FROM AccountJpa a WHERE a.id=:accountId AND a.deletedAt IS NULL AND a.validatedAt IS NULL")
    Optional<Long> checkNotValidatedById(Long accountId);

    @Query("SELECT a FROM AccountJpa a WHERE a.deletedAt IS NULL AND a.validatedAt IS NOT NULL")
    Optional<AccountJpa> findAllActive();

    @Query("SELECT a FROM AccountJpa a WHERE a.validatedAt IS NOT NULL")
    List<AccountJpa> findAllActiveAndDeleted();


}
