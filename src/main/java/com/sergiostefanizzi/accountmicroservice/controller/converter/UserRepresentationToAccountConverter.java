package com.sergiostefanizzi.accountmicroservice.controller.converter;

import com.sergiostefanizzi.accountmicroservice.model.Account;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class UserRepresentationToAccountConverter implements Converter<UserRepresentation, Account> {
    @Override
    public Account convert(UserRepresentation source) {
        Account account = new Account(
                source.getEmail(),
                source.getAttributes().get("birthdate") == null ? null : LocalDate.parse(source.getAttributes().get("birthdate").get(0)),
                source.getAttributes().get("gender") == null ? null : Account.GenderEnum.valueOf(source.getAttributes().get("gender").get(0)),
                null
        );
        account.setName(source.getFirstName());
        account.setSurname(source.getLastName());
        account.setId(source.getId());
        return account;
    }

}
