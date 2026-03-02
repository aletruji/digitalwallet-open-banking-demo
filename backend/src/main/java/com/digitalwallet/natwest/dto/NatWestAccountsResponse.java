package com.oepfelbaum.digitalwallet.natwest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NatWestAccountsResponse(Data Data) {


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(List<Account> Account) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Account(
            String AccountId,
            String Currency,
            String AccountTypeCode,
            String Description,
            String Nickname

    ) {
    }
}
