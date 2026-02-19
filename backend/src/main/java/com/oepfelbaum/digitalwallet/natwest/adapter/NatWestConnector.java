package com.oepfelbaum.digitalwallet.natwest.adapter;

import com.oepfelbaum.digitalwallet.natwest.dto.NatWestAccountsResponse;
import com.oepfelbaum.digitalwallet.natwest.service.NatWestAuthService;
import com.oepfelbaum.digitalwallet.natwest.service.NatWestDataService;
import com.oepfelbaum.digitalwallet.wallet.port.WalletConnector;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class NatWestConnector implements WalletConnector {

    private final NatWestAuthService auth;
    private final NatWestDataService data;

    public NatWestConnector(NatWestAuthService auth, NatWestDataService data) {
        this.auth = auth;
        this.data = data;
    }

    @Override
    public boolean isConnected() {
        return auth.isConnected();
    }

    @Override
    public void connect() {
        auth.connect();
    }

    @Override
    public String providerName() {
        return "NatWest";
    }

    @Override
    public void disconnect() {
        auth.disconnect();
    }

    @Override
    public List<Account> accounts() {
        var res = data.accounts();
        if (res == null || res.Data() == null || res.Data().Account() == null) return List.of();

        return res.Data().Account().stream()
                .map(a -> new Account(
                        a.AccountId(),
                        pickAccountName(a),
                        mapAccountTypeCode(a.AccountTypeCode()),
                        a.Currency() != null ? a.Currency() : "GBP"
                ))
                .toList();
    }

    private static String pickAccountName(NatWestAccountsResponse.Account a) {
        String nickname = a.Nickname();
        if (nickname != null && !nickname.isBlank()) return nickname;


        return "Account";
    }

    @Override
    public BigDecimal balance(String accountId) {
        var res = data.balances(accountId);
        var balances = (res != null && res.Data() != null) ? res.Data().Balance() : null;
        if (balances == null || balances.isEmpty()) return BigDecimal.ZERO;

        var first = balances.getFirst();
        var amountObj = first.Amount();
        BigDecimal amount = safeBigDecimal(amountObj != null ? amountObj.Amount() : null);

        // it is delivering Debit as a positive number, so negate
        if ("Debit".equalsIgnoreCase(first.CreditDebitIndicator())) amount = amount.negate();
        return amount;
    }

    @Override
    public List<WalletConnector.Transaction> transactions(String accountId) {
        var res = data.transactions(accountId);

        List<com.oepfelbaum.digitalwallet.natwest.dto.NatWestTransactionsResponse.TransactionItem> list =
                (res != null && res.Data() != null && res.Data().Transaction() != null)
                        ? res.Data().Transaction()
                        : List.of();

        return list.stream()
                .map(t -> new WalletConnector.Transaction(
                        t.BookingDateTime(),
                        t.ValueDateTime(),
                        t.CreditDebitIndicator(),
                        safeBigDecimal(t.Amount() != null ? t.Amount().Amount() : null),
                        t.Amount() != null ? t.Amount().Currency() : null,
                        t.TransactionInformation()
                ))
                .toList();
    }

    private static BigDecimal safeBigDecimal(String s) {
        if (s == null || s.isBlank()) return BigDecimal.ZERO;
        try { return new BigDecimal(s); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private static String mapAccountTypeCode(String code) {
        if (code == null || code.isBlank()) return null;

        return switch (code.toUpperCase()) {
            case "CACC" -> "Current";
            case "SVGS" -> "Savings";
            default -> code;
        };
    }


}
