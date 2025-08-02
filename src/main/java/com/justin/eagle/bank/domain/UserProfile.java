package com.justin.eagle.bank.domain;

import lombok.Builder;

@Builder
public record UserProfile( String name, String emailAddress, String phoneNumber) {
}
