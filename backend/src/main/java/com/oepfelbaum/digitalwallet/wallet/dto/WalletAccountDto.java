package com.oepfelbaum.digitalwallet.wallet.dto;

import java.math.BigDecimal;

public record WalletAccountDto(
        String accountId,
        String bankName,
        String accountName,
        String accountSubType,
        String currency,
        BigDecimal balance,
        boolean isNegative
) {}