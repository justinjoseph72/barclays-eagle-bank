package com.justin.eagle.bank.rest.controller;

import java.util.List;

import com.justin.eagle.bank.account.AccountService;
import com.justin.eagle.bank.auth.AuthenticateUserService;
import com.justin.eagle.bank.domain.ActiveAccount;
import com.justin.eagle.bank.domain.ApprovedTransaction;
import com.justin.eagle.bank.domain.PendingAccount;
import com.justin.eagle.bank.domain.TransactionRequest;
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
import com.justin.eagle.bank.rest.mappers.TransactionMapper;
import com.justin.eagle.bank.rest.mappers.UserMapper;
import com.justin.eagle.bank.transaction.TransactionService;
import com.justin.eagle.bank.user.UserService;
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

    @Autowired UserService userService;

    @Autowired AccountService accountService;

    @Autowired UserMapper userMapper;

    @Autowired AccountMapper accountMapper;

    @Autowired TransactionService transactionService;

    @Autowired TransactionMapper transactionMapper;

    @Override
    public ResponseEntity<BankAccountResponse> createAccount(
            @NotNull @Parameter(name = "Authorization", description = "Bearer JWT", required = true, in = ParameterIn.HEADER)
            @RequestHeader(value = "Authorization", required = true) String authorization,
            @Parameter(name = "CreateBankAccountRequest", description = "Create a new bank account for the user", required = true)
            @Valid @RequestBody CreateBankAccountRequest createBankAccountRequest) {
        final String authorizedUserId = authenticateUserService.findUserIdFromAuthToken(authorization);
        PendingAccount pendingAccount = accountMapper.buildPendingAccount(authorizedUserId, createBankAccountRequest);
        final ActiveAccount newAccount = accountService.createNewAccount(pendingAccount);
        return new ResponseEntity<>(accountMapper.buildAccountResponse(newAccount), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<TransactionResponse> createTransaction(
            @Pattern(regexp = "^01\\d{6}$") @Parameter(name = "accountNumber", description = "Account number of the bank account", required = true, in = ParameterIn.PATH) @PathVariable("accountNumber") String accountNumber,
            @NotNull @Parameter(name = "Authorization", description = "Bearer JWT", required = true, in = ParameterIn.HEADER) @RequestHeader(value = "Authorization", required = true) String authorization,
            @Parameter(name = "CreateTransactionRequest", description = "Create a new transaction", required = true) @Valid @RequestBody CreateTransactionRequest createTransactionRequest) {

        final String authorizedUserId = authenticateUserService.findUserIdFromAuthToken(authorization);
        TransactionRequest request = transactionMapper.buildTransactionRequest(authorizedUserId, accountNumber, createTransactionRequest);
        ApprovedTransaction transaction = transactionService.create(request);
        return new ResponseEntity<>(transactionMapper.buildTransactionResponse(transaction), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<UserResponse> createUser(@Parameter(name = "CreateUserRequest", description = "Create a new user", required = true)
    @Valid @RequestBody CreateUserRequest createUserRequest) {

        final ProvisionedUser user = userService.createUser(userMapper.createNewUser(createUserRequest));
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
    public ResponseEntity<BankAccountResponse> fetchAccountByAccountNumber(
            @Pattern(regexp = "^01\\d{6}$")
            @Parameter(name = "accountNumber", description = "Account number of the bank account", required = true, in = ParameterIn.PATH)
            @PathVariable("accountNumber") String accountNumber,
            @NotNull @Parameter(name = "Authorization", description = "Bearer JWT", required = true, in = ParameterIn.HEADER)
            @RequestHeader(value = "Authorization", required = true) String authorization) {
        final String authorizedUserId = authenticateUserService.findUserIdFromAuthToken(authorization);
        final ActiveAccount activeAccount = accountService.fetchAccountDetails(accountNumber, authorizedUserId);
        return new ResponseEntity<>(accountMapper.buildAccountResponse(activeAccount), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<TransactionResponse> fetchAccountTransactionByID(
            @Pattern(regexp = "^01\\d{6}$") @Parameter(name = "accountNumber", description = "Account number of the bank account", required = true, in = ParameterIn.PATH)
            @PathVariable("accountNumber") String accountNumber,
            @Pattern(regexp = "^tan-[A-Za-z0-9]$") @Parameter(name = "transactionId", description = "ID of the transaction", required = true, in = ParameterIn.PATH)
            @PathVariable("transactionId") String transactionId,
            @NotNull @Parameter(name = "Authorization", description = "Bearer JWT", required = true, in = ParameterIn.HEADER)
            @RequestHeader(value = "Authorization", required = true) String authorization
    ) {
        final String authorizedUserId = authenticateUserService.findUserIdFromAuthToken(authorization);
        final ApprovedTransaction transaction = transactionService.fetchTransaction(authorizedUserId, accountNumber, transactionId);
        return new ResponseEntity<>(transactionMapper.buildTransactionResponse(transaction), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UserResponse> fetchUserByID(@Pattern(regexp = "^usr-[A-Za-z0-9]+$")
            @Parameter(name = "userId", description = "ID of the user", required = true, in = ParameterIn.PATH)
            @PathVariable("userId") String userId,
            @NotNull @Parameter(name = "Authorization", description = "Bearer JWT", required = true, in = ParameterIn.HEADER)
            @RequestHeader(value = "Authorization", required = true) String authorization) {

        final String authorizedUserId = authenticateUserService.authorizeRequest(userId, authorization);

        return userService.fetchUser(authorizedUserId)
                .map(userMapper::buildUserResponse)
                .map(userResponse -> new ResponseEntity<>(userResponse, HttpStatus.OK))
                .orElseThrow(() -> {
                    log.error("no data for user '{}' found", userId);
                    return new UserNotFoundException();
                });
    }

    @Override
    public ResponseEntity<ListTransactionsResponse> listAccountTransaction(
            @Pattern(regexp = "^01\\d{6}$") @Parameter(name = "accountNumber", description = "Account number of the bank account", required = true, in = ParameterIn.PATH)
            @PathVariable("accountNumber") String accountNumber,
            @NotNull @Parameter(name = "Authorization", description = "Bearer JWT", required = true, in = ParameterIn.HEADER)
            @RequestHeader(value = "Authorization", required = true) String authorization) {
        final String authorizedUserId = authenticateUserService.findUserIdFromAuthToken(authorization);
        final List<ApprovedTransaction> approvedTransactions = transactionService.fetchTransactions(authorizedUserId, accountNumber);

        return new ResponseEntity<>(transactionMapper.buildListTransactionsResponse(approvedTransactions), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ListBankAccountsResponse> listAccounts(
            @NotNull @Parameter(name = "Authorization", description = "Bearer JWT", required = true, in = ParameterIn.HEADER)
            @RequestHeader(value = "Authorization", required = true) String authorization) {
        final String authorizedUserId = authenticateUserService.findUserIdFromAuthToken(authorization);
        final List<ActiveAccount> activeAccounts = accountService.fetchAllAccountsForUser(authorizedUserId);
        final List<BankAccountResponse> bankAccounts = activeAccounts.stream()
                .map(accountMapper::buildAccountResponse)
                .toList();
        return new ResponseEntity<>(ListBankAccountsResponse.builder().accounts(bankAccounts).build(), HttpStatus.OK);
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
