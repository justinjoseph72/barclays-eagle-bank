package com.justin.eagle.bank.rest.controller;

import com.justin.eagle.bank.account.AccountViewForbiddenException;
import com.justin.eagle.bank.auth.TokenNotCreatedException;
import com.justin.eagle.bank.auth.UserNotAuthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice()
@Slf4j
public class V1ApiControllerAdvice {

    @ExceptionHandler(value = { UserNotAuthorizedException.class,
            TokenNotCreatedException.class, AccountViewForbiddenException.class})
    public ResponseEntity<String> handleAuthError(Exception exception, HttpServletRequest request) {
        log.warn("The user is not authorized to access the resource '{}'-'{}'", request.getMethod(), request.getRequestURI(), exception);
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<String> handleUnknownException(Exception exception) {
        log.warn("Unknown exception", exception);
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

    }
}
