package com.justin.eagle.bank.user.model;

import lombok.Builder;

@Builder
public record UserProfile( String name, String emailAddress, String phoneNumber) {
}
