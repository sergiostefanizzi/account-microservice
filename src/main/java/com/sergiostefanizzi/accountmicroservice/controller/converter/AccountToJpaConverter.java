package com.sergiostefanizzi.accountmicroservice.controller.converter;

import com.sergiostefanizzi.accountmicroservice.model.Account;
import com.sergiostefanizzi.accountmicroservice.repository.model.AccountJpa;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class AccountToJpaConverter implements Converter<Account, AccountJpa> {
    @Override
    public AccountJpa convert(Account source) {
        return new AccountJpa(source.getEmail(),
                source.getName(),
                source.getSurname(),
                source.getBirthdate(),
                AccountJpa.Gender.valueOf(source.getGender().toString()),
                source.getPassword());
    }

    public Account convertBack(AccountJpa source) {
        Account account = new Account(source.getEmail(),
                source.getBirthdate(),
                Account.GenderEnum.valueOf(source.getGender().toString()), null);
        account.setName(source.getName());
        account.setSurname(source.getSurname());
        account.setId(source.getId());
        return account;
    }
}
