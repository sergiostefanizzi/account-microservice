package com.sergiostefanizzi.accountmicroservice.controller.converter;


import com.sergiostefanizzi.accountmicroservice.model.Admin;
import com.sergiostefanizzi.accountmicroservice.repository.model.AdminJpa;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class AdminJpaToAdminConverter implements Converter<AdminJpa, Admin> {
    @Override
    public Admin convert(AdminJpa source) {
        return new Admin(source.getId());
    }
}
