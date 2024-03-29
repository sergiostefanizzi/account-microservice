package com.sergiostefanizzi.accountmicroservice.system.config;

import com.sergiostefanizzi.accountmicroservice.system.util.AccountInterceptor;
import com.sergiostefanizzi.accountmicroservice.system.util.AdminInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class AccountConfiguration implements WebMvcConfigurer {
    @Autowired
    private AccountInterceptor accountInterceptor;
    @Autowired
    private AdminInterceptor adminInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.accountInterceptor)
                .addPathPatterns("/accounts/**")
                .excludePathPatterns("/admins/**")
                .excludePathPatterns("/admins/accounts");
        registry.addInterceptor(this.adminInterceptor)
                .addPathPatterns("/admins/**")
                .addPathPatterns("/admins/accounts")
                .excludePathPatterns("/accounts/**");
    }




}
