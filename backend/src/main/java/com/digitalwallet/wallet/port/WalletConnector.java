package com.oepfelbaum.digitalwallet.wallet.port;

import java.math.BigDecimal;
import java.util.List;

public interface WalletConnector {

    String providerName();
    boolean isConnected();
    void connect();
    void disconnect();

    List<Account> accounts();
    BigDecimal balance(String accountId);
    List<Transaction> transactions(String accountId);


    record Account(
            String accountId,
            String accountName,
            String accountSubType,
            String currency
    ) {}

    record Transaction(
            String bookingDateTime,
            String valueDateTime,
            String creditDebitIndicator,
            BigDecimal amount,
            String currency,
            Object transactionInformation
    ) {}
}
