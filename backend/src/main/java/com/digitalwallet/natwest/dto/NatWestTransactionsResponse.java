package com.digitalwallet.natwest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NatWestTransactionsResponse(Data Data) {


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(List<TransactionItem> Transaction) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Amount(String Amount, String Currency) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TransactionItem(
            String TransactionId,
            String BookingDateTime,
            String ValueDateTime,
            String CreditDebitIndicator,
            Amount Amount,
            Object TransactionInformation
    ) {}


}
