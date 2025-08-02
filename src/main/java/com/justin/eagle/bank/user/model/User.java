package com.justin.eagle.bank.user.model;

public sealed interface User permits NewUser, ProvisionedUser{
}
