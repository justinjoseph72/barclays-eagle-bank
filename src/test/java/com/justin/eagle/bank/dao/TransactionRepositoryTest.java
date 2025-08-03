package com.justin.eagle.bank.dao;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.justin.eagle.bank.domain.AccountIdentifier;
import com.justin.eagle.bank.domain.Amount;
import com.justin.eagle.bank.domain.AuditData;
import com.justin.eagle.bank.domain.CreditTransaction;
import com.justin.eagle.bank.domain.DebitTransaction;
import com.justin.eagle.bank.domain.TransactionIdentifier;
import com.justin.eagle.bank.domain.UserIdentifier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

@ExtendWith(MockitoExtension.class)
class TransactionRepositoryTest {

    private final NamedParameterJdbcTemplate jdbcTemplate = Mockito.mock(NamedParameterJdbcTemplate.class);
    private final KeyHolder keyHolder = Mockito.mock(KeyHolder.class);

    private final TransactionRepository repository = new TransactionRepository(jdbcTemplate);

    @Captor ArgumentCaptor<String> updateSqlCaptor;
    @Captor ArgumentCaptor<String> insertLogSqlCaptor;
    @Captor ArgumentCaptor<String> lockSqlCaptor;
    @Captor ArgumentCaptor<MapSqlParameterSource> lockParamCapture;
    @Captor ArgumentCaptor<MapSqlParameterSource> updateBalanceParamCapture;
    @Captor ArgumentCaptor<MapSqlParameterSource> insertParamCapture;
    @Captor ArgumentCaptor<KeyHolder> keyHolderCaptor;

    @Test
    void verifyCreditTransactionSucceedWhenThereIsNoErrorInBalanceUpdate() {
        final CreditTransaction creditTransaction = getCreditTransaction();

        Mockito.when(jdbcTemplate.update(lockSqlCaptor.capture(), lockParamCapture.capture())).thenReturn(1);


        Mockito.when(jdbcTemplate.update(updateSqlCaptor.capture(), updateBalanceParamCapture.capture(), keyHolderCaptor.capture()))
                .thenAnswer(invocationOnMock -> {
                    Object[] args = invocationOnMock.getArguments();
                    Map<String, Object> keyMap = new HashMap<String, Object>();
                    keyMap.put("updatedAmount", "343.44");
                    ((GeneratedKeyHolder)args[2]).getKeyList().add(keyMap);
                    return 1;
                });

        Mockito.when(jdbcTemplate.update(insertLogSqlCaptor.capture(), insertParamCapture.capture())).thenReturn(1);


        repository.updateBalanceForTransaction(creditTransaction);

        final String updateSql = updateSqlCaptor.getValue();
        Assertions.assertThat(updateSql).contains("returning");
        Assertions.assertThat(updateSql).doesNotContain(">=");
        Assertions.assertThat(insertLogSqlCaptor.getValue()).startsWith("insert into transaction_log");
        final MapSqlParameterSource insertParams = insertParamCapture.getValue();
        Assertions.assertThat(insertParams.getValue("runningBalance")).isEqualTo(new BigDecimal("343.44"));
        Assertions.assertThat(insertParams.getValue("isCredit")).isEqualTo(Boolean.TRUE);
    }



    @Test
    void verifyDebitTransactionSucceedWhenThereIsNoErrorInBalanceUpdate() {
        final DebitTransaction debitTransaction = getDebitTransaction();

        Mockito.when(jdbcTemplate.update(lockSqlCaptor.capture(), lockParamCapture.capture())).thenReturn(1);


        Mockito.when(jdbcTemplate.update(updateSqlCaptor.capture(), updateBalanceParamCapture.capture(), keyHolderCaptor.capture()))
                .thenAnswer(invocationOnMock -> {
                    Object[] args = invocationOnMock.getArguments();
                    Map<String, Object> keyMap = new HashMap<String, Object>();
                    keyMap.put("updatedAmount", "343.44");
                    ((GeneratedKeyHolder) args[2]).getKeyList().add(keyMap);
                    return 1;
                });

        Mockito.when(jdbcTemplate.update(insertLogSqlCaptor.capture(), insertParamCapture.capture())).thenReturn(1);


        repository.updateBalanceForTransaction(debitTransaction);

        final String updateSql = updateSqlCaptor.getValue();
        Assertions.assertThat(updateSql).contains("returning");
        Assertions.assertThat(updateSql).contains(">=");
        Assertions.assertThat(insertLogSqlCaptor.getValue()).startsWith("insert into transaction_log");
        final MapSqlParameterSource insertParams = insertParamCapture.getValue();
        Assertions.assertThat(insertParams.getValue("runningBalance")).isEqualTo(new BigDecimal("343.44"));
        Assertions.assertThat(insertParams.getValue("isCredit")).isEqualTo(Boolean.FALSE);
    }


    @Test
    void verifyDebitTransactionFailsWhenBalanceUpdateFails() {
        final DebitTransaction debitTransaction = getDebitTransaction();

        Mockito.when(jdbcTemplate.update(lockSqlCaptor.capture(), lockParamCapture.capture())).thenReturn(1);


        Mockito.when(jdbcTemplate.update(updateSqlCaptor.capture(), updateBalanceParamCapture.capture(), keyHolderCaptor.capture()))
                .thenAnswer(invocationOnMock -> {
                    Object[] args = invocationOnMock.getArguments();
                    Map<String, Object> keyMap = new HashMap<String, Object>();
                    keyMap.put("updatedAmount", "343.44");
                    ((GeneratedKeyHolder)args[2]).getKeyList().add(keyMap);
                    return 0;
                });

        Mockito.when(jdbcTemplate.update(insertLogSqlCaptor.capture(), insertParamCapture.capture())).thenReturn(1);

        Assertions.assertThatThrownBy(() -> repository.updateBalanceForTransaction(debitTransaction))
                .isInstanceOf(BalanceUpdateException.class);

        final String updateSql = updateSqlCaptor.getValue();
        Assertions.assertThat(updateSql).contains("returning");
        Assertions.assertThat(updateSql).contains(">=");
    }

    private static CreditTransaction getCreditTransaction() {
        return CreditTransaction.builder()
                .transactionId(TransactionIdentifier.builder()
                        .externalId("someId")
                        .id(UUID.randomUUID())
                        .build())
                .userIdentifier(UserIdentifier.builder()
                        .partyId(UUID.randomUUID())
                        .externalUserId("someId")
                        .build())
                .accountIdentifier(AccountIdentifier.builder()
                        .id(UUID.randomUUID())
                        .accountNumber("someAccountNumber")
                        .sortCode("someCode")
                        .build())
                .transactionAmount(Amount.builder()
                        .amount(BigDecimal.TEN)
                        .currency("GBP")
                        .build())
                .auditData(AuditData.builder()
                        .createdTimestamp(Instant.now())
                        .lastUpdatedTimestamp(Instant.now())
                        .build())
                .build();
    }

    private static DebitTransaction getDebitTransaction() {
        return DebitTransaction.builder()
                .transactionId(TransactionIdentifier.builder()
                        .externalId("someId")
                        .id(UUID.randomUUID())
                        .build())
                .userIdentifier(UserIdentifier.builder()
                        .partyId(UUID.randomUUID())
                        .externalUserId("someId")
                        .build())
                .accountIdentifier(AccountIdentifier.builder()
                        .id(UUID.randomUUID())
                        .accountNumber("someAccountNumber")
                        .sortCode("someCode")
                        .build())
                .transactionAmount(Amount.builder()
                        .amount(BigDecimal.TEN)
                        .currency("GBP")
                        .build())
                .auditData(AuditData.builder()
                        .createdTimestamp(Instant.now())
                        .lastUpdatedTimestamp(Instant.now())
                        .build())
                .build();
    }
}