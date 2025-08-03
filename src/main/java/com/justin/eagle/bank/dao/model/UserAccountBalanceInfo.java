package com.justin.eagle.bank.dao.model;

import java.math.BigDecimal;

import lombok.Builder;

@Builder
public record UserAccountBalanceInfo(UserStatusDbInfo userInfo, AccountStatusInfo accountInfo, BigDecimal currentBalance, String currency) {
}
