package com.justin.eagle.bank.rest.controller;

import com.justin.eagle.bank.account.AccountCrudService;
import com.justin.eagle.bank.auth.AuthenticateUserService;
import com.justin.eagle.bank.domain.ActiveAccount;
import com.justin.eagle.bank.domain.PendingAccount;
import com.justin.eagle.bank.generated.openapi.rest.api.V1Api;
import com.justin.eagle.bank.generated.openapi.rest.model.BankAccountResponse;
import com.justin.eagle.bank.generated.openapi.rest.model.CreateBankAccountRequest;
import com.justin.eagle.bank.generated.openapi.rest.model.CreateTransactionRequest;
import com.justin.eagle.bank.generated.openapi.rest.model.CreateUserRequest;
import com.justin.eagle.bank.generated.openapi.rest.model.ListBankAccountsResponse;
import com.justin.eagle.bank.generated.openapi.rest.model.ListTransactionsResponse;
import com.justin.eagle.bank.generated.openapi.rest.model.TransactionResponse;
import com.justin.eagle.bank.generated.openapi.rest.model.UpdateBankAccountRequest;
import com.justin.eagle.bank.generated.openapi.rest.model.UpdateUserRequest;
import com.justin.eagle.bank.generated.openapi.rest.model.UserAuthResponse;
import com.justin.eagle.bank.generated.openapi.rest.model.UserResponse;
import com.justin.eagle.bank.rest.mappers.AccountMapper;
import com.justin.eagle.bank.rest.mappers.UserMapper;
import com.justin.eagle.bank.user.UserCrudService;
import com.justin.eagle.bank.domain.ProvisionedUser;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class V1ApiController implements V1Api {

    @Autowired
    private AuthenticateUserService authenticateUserService;

    @Autowired UserCrudService userCrudService;

    @Autowired AccountCrudService accountCrudService;

    @Autowired UserMapper userMapper;

    @Autowired AccountMapper accountMapper;

    @Override
    public ResponseEntity<BankAccountResponse> createAccount(
            @NotNull @Parameter(name = "Authorization", description = "Bearer JWT", required = true, in = ParameterIn.HEADER)
            @RequestHeader(value = "Authorization", required = true) String authorization,
            @Parameter(name = "CreateBankAccountRequest", description = "Create a new bank account for the user", required = true)
            @Valid @RequestBody CreateBankAccountRequest createBankAccountRequest) {
        final String authorizedUserId = authenticateUserService.findUserIdFromAuthToken(authorization);
        PendingAccount pendingAccount = accountMapper.buildPendingAccount(authorizedUserId, createBankAccountRequest);
        final ActiveAccount newAccount = accountCrudService.createNewAccount(pendingAccount);
        return new ResponseEntity<>(accountMapper.buildAccountResponse(newAccount), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<TransactionResponse> createTransaction(String accountNumber, CreateTransactionRequest createTransactionRequest) {
        return V1Api.super.createTransaction(accountNumber, createTransactionRequest);
    }

    @Override
    public ResponseEntity<UserResponse> createUser(@Parameter(name = "CreateUserRequest", description = "Create a new user", required = true)
    @Valid @RequestBody CreateUserRequest createUserRequest) {

        final ProvisionedUser user = userCrudService.createUser(userMapper.createNewUser(createUserRequest));
        return new ResponseEntity<>(userMapper.buildUserResponse(user), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteAccountByAccountNumber(String accountNumber) {
        return V1Api.super.deleteAccountByAccountNumber(accountNumber);
    }

    @Override
    public ResponseEntity<Void> deleteUserByID(String userId) {
        return V1Api.super.deleteUserByID(userId);
    }

    @Override
    public ResponseEntity<BankAccountResponse> fetchAccountByAccountNumber(String accountNumber) {
        return V1Api.super.fetchAccountByAccountNumber(accountNumber);
    }

    @Override
    public ResponseEntity<TransactionResponse> fetchAccountTransactionByID(String accountNumber, String transactionId) {
        return V1Api.super.fetchAccountTransactionByID(accountNumber, transactionId);
    }

    @Override
    public ResponseEntity<UserResponse> fetchUserByID(@Pattern(regexp = "^usr-[A-Za-z0-9]+$")
            @Parameter(name = "userId", description = "ID of the user", required = true, in = ParameterIn.PATH)
            @PathVariable("userId") String userId,
            @NotNull @Parameter(name = "Authorization", description = "Bearer JWT", required = true, in = ParameterIn.HEADER)
            @RequestHeader(value = "Authorization", required = true) String authorization) {

        final String authorizedUserId = authenticateUserService.authorizeRequest(userId, authorization);

        return userCrudService.fetchUser(authorizedUserId)
                .map(userMapper::buildUserResponse)
                .map(userResponse -> new ResponseEntity<>(userResponse, HttpStatus.OK))
                .orElseThrow(() -> {
                    log.error("no data for user '{}' found", userId);
                    return new UserNotFoundException();
                });
    }

    @Override
    public ResponseEntity<ListTransactionsResponse> listAccountTransaction(String accountNumber) {
        return V1Api.super.listAccountTransaction(accountNumber);
    }

    @Override
    public ResponseEntity<ListBankAccountsResponse> listAccounts() {
        return V1Api.super.listAccounts();
    }

    @Override
    public ResponseEntity<BankAccountResponse> updateAccountByAccountNumber(String accountNumber, UpdateBankAccountRequest updateBankAccountRequest) {
        return V1Api.super.updateAccountByAccountNumber(accountNumber, updateBankAccountRequest);
    }

    @Override
    public ResponseEntity<UserResponse> updateUserByID(String userId, UpdateUserRequest updateUserRequest) {
        return V1Api.super.updateUserByID(userId, updateUserRequest);
    }

    //Done need testing for unhappy path
    @Override
    public ResponseEntity<UserAuthResponse> authorizeUserId(
            @Pattern(regexp = "^usr-[A-Za-z0-9]+$") @Parameter(name = "userId", description = "ID of the user", required = true, in = ParameterIn.PATH) @PathVariable("userId") String userId) {

        final UserAuthResponse userAuthResponse = authenticateUserService.buildNewAuthToken(userId);
        return new ResponseEntity<>(userAuthResponse, HttpStatus.CREATED);
    }
}
