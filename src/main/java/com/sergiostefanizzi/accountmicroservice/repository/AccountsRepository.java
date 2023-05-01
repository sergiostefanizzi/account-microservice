package com.sergiostefanizzi.accountmicroservice.repository;

import com.sergiostefanizzi.accountmicroservice.repository.model.AccountJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountsRepository extends JpaRepository<AccountJpa, Long> {
    //Verifica che l'email passata come parametro è già stata registrata, nel caso ritorna l'intero Account
    //Se c'è un match viene ritornato l'oggetto Optional
    Optional<AccountJpa> findByEmail(String email);


}
