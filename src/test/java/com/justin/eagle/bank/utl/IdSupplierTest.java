package com.justin.eagle.bank.utl;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class IdSupplierTest {

    IdSupplier idSupplier = new IdSupplier();

    Predicate<String> accountNumberPattern = Pattern.compile("^01\\d{6}$").asMatchPredicate();

    @ParameterizedTest
    @ValueSource(ints = {7,12,134,1345,23456,543456,000001,999999})
    void shouldCheckTheAccountNumberGeneratedIsMax8CharsLong(int value) {
        final String newAccountNumber = idSupplier.getNewAccountNumber(value);
        Assertions.assertThat(newAccountNumber.length()).isEqualTo(8);
        Assertions.assertThat(accountNumberPattern.test(newAccountNumber)).isTrue();
    }
}