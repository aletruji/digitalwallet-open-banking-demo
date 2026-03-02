package com.digitalwallet.wallet.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WalletTransactionDto(
        LocalDate date,
        BigDecimal amount,
        String currency,
        String counterparty,
        boolean isNegative
) {}