package com.digitalwallet.natwest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NatWestBalancesResponse(Data Data) {


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(List<Balance> Balance) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Amount(String Amount, String Currency) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Balance(
            Amount Amount,
            String CreditDebitIndicator,
            String Type
    ) {}


}
