package com.sergiostefanizzi.accountmicroservice.controller.converter;


import com.sergiostefanizzi.accountmicroservice.model.Admin;
import com.sergiostefanizzi.accountmicroservice.repository.model.AccountJpa;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class AccountJpaToAdminConverter implements Converter<AccountJpa, Admin> {
    @Override
    public Admin convert(AccountJpa source) {
        return new Admin(source.getId());
    }
}
