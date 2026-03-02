package com.digitalwallet.wallet.service;

import com.digitalwallet.wallet.domain.MoneyRounder;
import com.digitalwallet.wallet.dto.WalletAccountDetailsDto;
import com.digitalwallet.wallet.dto.WalletAccountDto;
import com.digitalwallet.wallet.dto.WalletOverviewDto;
import com.digitalwallet.wallet.dto.WalletTransactionDto;
import com.digitalwallet.wallet.port.WalletConnector;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class WalletService {

    private final WalletConnector connector;
    private static final Logger log = LoggerFactory.getLogger(WalletService.class);


    public WalletService(WalletConnector connector) {
        this.connector = connector;
    }


    public WalletOverviewDto getOverview() {
        log.info("Wallet overview requested");

        List<WalletConnector.Account> accounts = connector.accounts();

        List<WalletAccountDto> result = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (var acc : accounts) {
            String accountId = acc.accountId();
            String accountSubType = acc.accountSubType();
            String accountName = acc.accountName();
            String currency = acc.currency();

            BigDecimal balance = MoneyRounder.roundToFiveRappen(connector.balance(accountId));
            boolean isNegative = MoneyRounder.isNegative(balance);

            total = total.add(balance);

            result.add(new WalletAccountDto(
                    accountId,
                    connector.providerName(),
                    accountName,
                    accountSubType,
                    currency,
                    balance,
                    isNegative
            ));
        }

        BigDecimal totalRounded = MoneyRounder.roundToFiveRappen(total);
        return new WalletOverviewDto(result, totalRounded, MoneyRounder.isNegative(totalRounded));
    }


    public WalletAccountDetailsDto getAccountDetails(String accountId, String sort) {

        log.info("Wallet account details requested accountId={} sort={}", accountId, sort);

        WalletConnector.Account account = findAccount(accountId);

        BigDecimal balance = MoneyRounder.roundToFiveRappen(connector.balance(accountId));
        boolean isNegative = MoneyRounder.isNegative(balance);

        List<WalletTransactionDto> tx = fetchTransactionsLast30Days(accountId);

        Comparator<WalletTransactionDto> cmp = Comparator.comparing(WalletTransactionDto::date);
        if (sort != null && sort.equalsIgnoreCase("desc")) {
            cmp = cmp.reversed();
        }
        tx.sort(cmp);

        return new WalletAccountDetailsDto(
                accountId,
                connector.providerName(),
                account.accountName(),
                account.accountSubType(),
                account.currency(),
                balance,
                isNegative,
                tx
        );
    }


    private WalletConnector.Account findAccount(String accountId) {
        return connector.accounts().stream()
                .filter(a -> accountId != null && accountId.equals(a.accountId()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Account not found: " + accountId));
    }


    private List<WalletTransactionDto> fetchTransactionsLast30Days(String accountId) {
        List<WalletConnector.Transaction> txList = connector.transactions(accountId);

        LocalDate cutoff = LocalDate.now().minusDays(30);

        List<WalletTransactionDto> out = new ArrayList<>();
        for (var t : txList) {
            LocalDate date = parseTxDate(t.bookingDateTime(), t.valueDateTime());
            if (date == null || date.isBefore(cutoff)) continue;

            BigDecimal amount = t.amount() != null ? t.amount() : BigDecimal.ZERO;
            if ("Debit".equalsIgnoreCase(t.creditDebitIndicator())) {
                amount = amount.negate();
            }
            amount = MoneyRounder.roundToFiveRappen(amount);

            String counterparty = pickCounterparty(t.transactionInformation());
            boolean isNegative = MoneyRounder.isNegative(amount);

            out.add(new WalletTransactionDto(date, amount, t.currency(), counterparty, isNegative));
        }
        return out;
    }


    private LocalDate parseTxDate(String bookingDateTime, String valueDateTime) {
        String s = bookingDateTime != null ? bookingDateTime : valueDateTime;
        if (s == null) return null;

        try {
            if (s.contains("T")) {
                return OffsetDateTime.parse(s, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate();
            }
        } catch (Exception ignored) {}

        try {
            return LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception ignored) {}

        return null;
    }


    private String pickCounterparty(Object info) {
        switch (info) {
            case null -> {
                return "Unknown";
            }
            case String s when !s.isBlank() -> {
                return s;
            }
            case List<?> list when !list.isEmpty() -> {
                String s = String.valueOf(list.getFirst());
                if (s != null && !s.isBlank()) return s;
            }
            default -> {
            }
        }

        if (info instanceof java.util.Map<?, ?> m && !m.isEmpty()) {
            Object first = m.values().iterator().next();
            if (first != null) {
                String s = String.valueOf(first);
                if (!s.isBlank()) return s;
            }
        }

        return "Unknown";
    }


    public boolean isConnected() {
        return connector.isConnected();
    }


    public void connect() {
        connector.connect();
    }


    public void disconnect() {
        connector.disconnect();
    }

}
