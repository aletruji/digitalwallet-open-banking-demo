package com.oepfelbaum.digitalwallet.wallet.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyRounder {

    private static final BigDecimal STEP = new BigDecimal("0.05");

    private MoneyRounder() {}

    public static BigDecimal roundToFiveRappen(BigDecimal amount) {
        if (amount == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        BigDecimal rounded = amount
                .divide(STEP, 0, RoundingMode.HALF_UP)
                .multiply(STEP);

        return rounded.setScale(2, RoundingMode.HALF_UP);
    }

    public static boolean isNegative(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) < 0;
    }
}