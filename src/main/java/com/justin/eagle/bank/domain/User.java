package com.justin.eagle.bank.domain;

public sealed interface User permits UserInfo, ProvisionedUser{
}
