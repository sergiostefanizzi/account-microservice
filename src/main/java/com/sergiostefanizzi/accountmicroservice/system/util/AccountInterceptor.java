package com.sergiostefanizzi.accountmicroservice.system.util;

import com.sergiostefanizzi.accountmicroservice.repository.AccountsRepository;
import com.sergiostefanizzi.accountmicroservice.service.KeycloakService;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AccountAlreadyActivatedException;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.AccountNotFoundException;
import com.sergiostefanizzi.accountmicroservice.system.exceptions.ActionForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
public class AccountInterceptor implements HandlerInterceptor {
    @Autowired
    private KeycloakService keycloakService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("\n\tAccount Interceptor -> "+request.getRequestURI());
        // Esco se e' un metodo post
        if (request.getMethod().equalsIgnoreCase("POST") || request.getMethod().equalsIgnoreCase("GET")) return true;

        Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        String accountId = (String) pathVariables.get("accountId");
        Boolean check = false;
        String requestUri = request.getRequestURI();
        if(request.getMethod().equalsIgnoreCase("PUT") && requestUri.equalsIgnoreCase("/accounts/"+accountId)){
            check = keycloakService.checkActiveById(accountId);
            if(check && !keycloakService.checksEmailValidated(accountId)){
                throw new AccountAlreadyActivatedException(accountId);
            }
        }else {
            check = keycloakService.checkActiveById(accountId);
        }
        
        if (!check){
            throw new AccountNotFoundException(accountId);
        }
        //TODO Farlo prima
        String tokenAccountId = getJwtAccountId();
        if (!accountId.equals(tokenAccountId) && !requestUri.equalsIgnoreCase("/admins/"+accountId)){
            throw new ActionForbiddenException(tokenAccountId);
        }

        log.info("\n\tAccount Interceptor: Account ID-> "+accountId);
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

    private static String getJwtAccountId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JwtAuthenticationToken oauthToken = (JwtAuthenticationToken) authentication;
        String jwtAccountId = oauthToken.getToken().getClaim("sub");
        log.info("TOKEN ACCOUNT ID --> "+jwtAccountId);
        return jwtAccountId;
    }
}
