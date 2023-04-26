package com.sergiostefanizzi.accountmicroservice.controller.converter;

import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.repository.model.AccountJpa;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class AccountJpaToAccountConverter implements Converter<AccountJpa, Account> {
    @Override
    public Account convert(AccountJpa source) {
        Account account = new Account(source.getEmail(),
                source.getBirthdate(),
                Account.GenderEnum.valueOf(source.getGender().toString()),
                source.getPassword());
        account.setName(source.getName());
        account.setSurname(source.getSurname());
        account.setId(source.getId());
        return account;
    }
}
