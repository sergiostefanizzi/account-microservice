package com.sergiostefanizzi.accountmicroservice.controller.converter;

import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.model.AccountJpa;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class AccountToJpaConverter implements Converter<Account, AccountJpa> {
    @Override
    public AccountJpa convert(Account source) {
        AccountJpa accountJpa = new AccountJpa(source.getEmail(),
                source.getBirthdate(),
                source.getGender(),
                source.getPassword());
        if (source.getName() != null) accountJpa.setName(source.getName());
        if (source.getSurname() != null) accountJpa.setSurname(source.getSurname());
        return accountJpa;
    }

    public Account convertBack(AccountJpa source) {
        Account account = new Account(source.getEmail(),
                source.getBirthdate(),
                source.getGender(), null);
        account.setName(source.getName());
        account.setSurname(source.getSurname());
        account.setId(source.getId());
        return account;
    }
}
