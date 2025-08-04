package com.justin.eagle.bank.transaction.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TransactionTypeTest {

    @Test
    void verifyCreditEnumIsCorrectlyParsed() {
        final TransactionType deposit = TransactionType.fromValue("deposit");
        Assertions.assertEquals(TransactionType.CREDIT, deposit);
    }

    @Test
    void verifyDebitEnumIsCorrectlyParsed() {
        final TransactionType withdrawal = TransactionType.fromValue("withdrawal");
        Assertions.assertEquals(TransactionType.DEBIT, withdrawal);
    }

    @Test
    void verifyUnknownValueWillThrowException() {
        Assertions.assertThrows(IllegalStateException.class, () -> TransactionType.fromValue("withdraw"));
    }

}