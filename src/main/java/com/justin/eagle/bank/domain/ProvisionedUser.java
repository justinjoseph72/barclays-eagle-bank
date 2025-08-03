package com.justin.eagle.bank.domain;

import lombok.Builder;

@Builder
public record ProvisionedUser(UserIdentifier identifier, UserInfo info, AuditData auditData) implements User{
}
