package com.oepfelbaum.digitalwallet.wallet.dto;

import java.math.BigDecimal;
import java.util.List;

public record WalletAccountDetailsDto(
        String accountId,
        String bankName,
        String accountName,
        String accountSubType,
        String currency,
        BigDecimal balance,
        boolean isNegative,
        List<WalletTransactionDto> transactions
) {}