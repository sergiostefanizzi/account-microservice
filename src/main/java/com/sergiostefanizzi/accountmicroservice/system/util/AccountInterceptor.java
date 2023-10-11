package com.sergiostefanizzi.accountmicroservice.system.util;

import com.sergiostefanizzi.accountmicroservice.repository.AccountsRepository;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AccountNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Slf4j
@Component
public class AccountInterceptor implements HandlerInterceptor {
    @Autowired
    private AccountsRepository accountsRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("\n\tAccount Interceptor -> "+request.getRequestURI());
        // Esco se e' un metodo post
        if (request.getMethod().equalsIgnoreCase("POST") || request.getMethod().equalsIgnoreCase("GET")) return true;

        Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        Long accountId = Long.valueOf((String) pathVariables.get("accountId"));
        Long checkId;
        if(request.getMethod().equalsIgnoreCase("PUT") && request.getRequestURI().equalsIgnoreCase("/accounts/"+accountId)){
            checkId = this.accountsRepository.checkNotValidatedById(accountId)
                    .orElseThrow(() -> new AccountNotFoundException(accountId));
        }else {
            checkId = this.accountsRepository.checkActiveById(accountId)
                    .orElseThrow(() -> new AccountNotFoundException(accountId));
        }


        log.info("\n\tAccount Interceptor: Profile ID-> "+checkId);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
