package com.justin.eagle.bank.domain;

import java.util.List;

import lombok.Builder;

@Builder
public record UserAddress(List<String> addressLines, String county, String town, String postCode) {
}
