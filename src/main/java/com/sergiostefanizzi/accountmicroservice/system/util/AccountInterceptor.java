package com.sergiostefanizzi.accountmicroservice.system.util;

import com.sergiostefanizzi.accountmicroservice.service.KeycloakService;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountInterceptor implements HandlerInterceptor {
    private final KeycloakService keycloakService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("\n\tAccount Interceptor -> "+request.getRequestURI());
        // Esco se e' un metodo post
        if (request.getMethod().equalsIgnoreCase("POST") || request.getMethod().equalsIgnoreCase("GET")) return true;

        Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        String accountId = (String) pathVariables.get("accountId");
        log.info("\n\tAccount Interceptor: Account ID-> "+accountId);

        String tokenAccountId = JwtUtilityClass.getJwtAccountId();
        if (!accountId.equals(tokenAccountId)){
            throw new ActionForbiddenException(tokenAccountId);
        }
        Boolean isActive = keycloakService.checkActiveById(accountId);
        if(Boolean.TRUE.equals(isActive)){
            if(Boolean.TRUE.equals(keycloakService.checksEmailValidated(accountId))){
                if(request.getMethod().equalsIgnoreCase("PUT")){
                    throw new AccountAlreadyActivatedException(accountId);
                }
            }else {
                if(!request.getMethod().equalsIgnoreCase("PUT")){
                    throw new EmailNotValidatedException(accountId);
                }
            }
        }else {
            throw new AccountNotFoundException(accountId);
        }
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
