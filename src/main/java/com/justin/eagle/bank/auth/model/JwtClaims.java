package com.justin.eagle.bank.auth.model;

import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Builder
public class JwtClaims {

    @JsonProperty("sub")
    String subject;

    @JsonProperty("iat")
    Long createdAt;

    @JsonProperty("exp")
    Long expiringAt;

    @JsonProperty("iss")
    String issuer;

    @JsonProperty("typ")
    String type;
}
