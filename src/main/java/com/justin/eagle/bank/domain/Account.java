package com.justin.eagle.bank.domain;

public sealed interface Account permits PendingAccount, ActiveAccount{
}
