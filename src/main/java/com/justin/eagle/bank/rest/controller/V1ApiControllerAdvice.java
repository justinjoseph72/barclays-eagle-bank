package com.justin.eagle.bank.rest.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.justin.eagle.bank.account.AccountViewForbiddenException;
import com.justin.eagle.bank.account.NoAccountFoundException;
import com.justin.eagle.bank.auth.TokenNotCreatedException;
import com.justin.eagle.bank.auth.UserNotAuthorizedException;
import com.justin.eagle.bank.dao.BalanceUpdateException;
import com.justin.eagle.bank.generated.openapi.rest.model.BadRequestErrorResponse;
import com.justin.eagle.bank.generated.openapi.rest.model.BadRequestErrorResponseDetailsInner;
import com.justin.eagle.bank.generated.openapi.rest.model.ErrorResponse;
import com.justin.eagle.bank.transaction.FailedTransactionException;
import com.justin.eagle.bank.transaction.TransactionNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice()
@Slf4j
public class V1ApiControllerAdvice {

    @ExceptionHandler(value = { UserNotAuthorizedException.class,
            TokenNotCreatedException.class, AccountViewForbiddenException.class })
    public ResponseEntity<String> handleAuthError(Exception exception, HttpServletRequest request) {
        log.warn("The user is not authorized to access the resource '{}'-'{}'", request.getMethod(), request.getRequestURI(), exception);
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = { UserNotFoundException.class, NoAccountFoundException.class, TransactionNotFoundException.class })
    public ResponseEntity<ErrorResponse> handleNotFoundException(Exception exception, HttpServletRequest request) {
        log.warn("unexpected error happened with executing '{} {}'", request.getMethod(), request.getRequestURI(), exception);
        return new ResponseEntity<>(ErrorResponse.builder()
                .message(exception.getMessage())
                .build(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = { BalanceUpdateException.class, FailedTransactionException.class })
    public ResponseEntity<ErrorResponse> handleTransactionErrors(Exception exception) {
        log.warn("Unknown exception", exception);
        return new ResponseEntity<>(ErrorResponse.builder()
                .message(exception.getMessage())
                .build(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<BadRequestErrorResponse> handleException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        final List<BadRequestErrorResponseDetailsInner> details = exception.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> BadRequestErrorResponseDetailsInner.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .type(error.getObjectName())
                        .build())
                .toList();
        final BadRequestErrorResponse payloadValidationFailure = BadRequestErrorResponse.builder()
                .message("Payload validation failure")
                .details(details)
                .build();
        log.warn("request for resource '{} {}' failed with Bad request error due to error in field '{}'", request.getMethod(), request.getRequestURI(),
                details.stream().map(BadRequestErrorResponseDetailsInner::getField).collect(Collectors.joining(",")));
        return new ResponseEntity<>(payloadValidationFailure, HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResponseEntity<BadRequestErrorResponse> handleException(ConstraintViolationException exception, HttpServletRequest request) {
        final List<BadRequestErrorResponseDetailsInner> details = exception.getConstraintViolations().stream()
                .map(error -> BadRequestErrorResponseDetailsInner.builder()
                        .field(error.getPropertyPath().toString())
                        .message(error.getMessage())
                        .type(error.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName())
                        .build())
                .toList();

        final BadRequestErrorResponse payloadValidationFailure = BadRequestErrorResponse.builder()
                .message("Field Validation failure")
                .details(details)
                .build();
        log.warn("request for resource '{} {}' failed with Bad request error due to error '{}'", request.getMethod(), request.getRequestURI(),
                details.stream().map(BadRequestErrorResponseDetailsInner::getField).collect(Collectors.joining(",")));
        return new ResponseEntity<>(payloadValidationFailure, HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ResponseEntity<BadRequestErrorResponse> handleException(HttpMessageNotReadableException exception, HttpServletRequest request) {

        final BadRequestErrorResponse payloadValidationFailure = BadRequestErrorResponse.builder()
                .message(exception.getMessage())
                .details(List.of())
                .build();
        log.warn("request for resource '{} {}' failed with Bad request as the payload is not readable", request.getMethod(), request.getRequestURI());
        return new ResponseEntity<>(payloadValidationFailure, HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknownException(Exception exception, HttpServletRequest request) {
        log.warn("unexpected error happened with executing '{} {}'", request.getMethod(), request.getRequestURI(), exception);
        return new ResponseEntity<>(ErrorResponse.builder()
                .message("unexpected error")
                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
