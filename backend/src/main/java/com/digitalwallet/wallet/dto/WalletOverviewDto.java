package com.digitalwallet.wallet.dto;

import java.math.BigDecimal;
import java.util.List;

public record WalletOverviewDto(
        List<WalletAccountDto> accounts,
        BigDecimal totalAmount,
        boolean totalIsNegative
) {}