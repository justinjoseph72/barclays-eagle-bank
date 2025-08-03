package com.justin.eagle.bank.domain;

import org.springframework.validation.annotation.Validated;

public sealed interface Transaction permits TransactionRequest, ApprovedTransaction {
}
