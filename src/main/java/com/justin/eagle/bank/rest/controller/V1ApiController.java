package com.justin.eagle.bank.rest.controller;

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
import com.justin.eagle.bank.generated.openapi.rest.model.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class V1ApiController implements V1Api {


    @Override
    public ResponseEntity<BankAccountResponse> createAccount(CreateBankAccountRequest createBankAccountRequest) {
        return V1Api.super.createAccount(createBankAccountRequest);
    }

    @Override
    public ResponseEntity<TransactionResponse> createTransaction(String accountNumber, CreateTransactionRequest createTransactionRequest) {
        return V1Api.super.createTransaction(accountNumber, createTransactionRequest);
    }

    @Override
    public ResponseEntity<UserResponse> createUser(CreateUserRequest createUserRequest) {
        return V1Api.super.createUser(createUserRequest);
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
    public ResponseEntity<UserResponse> fetchUserByID(String userId) {
        return V1Api.super.fetchUserByID(userId);
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
}
