package com.oepfelbaum.digitalwallet.wallet.domain;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class MoneyRounderTest {

    @Test
    public void roundToFiveRappen_null_returnsZeroWithTwoDecimals() {
        BigDecimal out = MoneyRounder.roundToFiveRappen(null);
        assertEquals(new BigDecimal("0.00"), out);
    }

    @Test
    public void roundToFiveRappen_exactStep_staysSame() {
        BigDecimal out = MoneyRounder.roundToFiveRappen(new BigDecimal("1.10"));
        assertEquals(new BigDecimal("1.10"), out);

        BigDecimal out2 = MoneyRounder.roundToFiveRappen(new BigDecimal("1.15"));
        assertEquals(new BigDecimal("1.15"), out2);
    }

    @Test
    public void roundToFiveRappen_roundsDownWhenCloserToLowerStep() {
        // 1.12 is closer to 1.10 than to 1.15 with HALF_UP rounding on 0.05 steps
        BigDecimal out = MoneyRounder.roundToFiveRappen(new BigDecimal("1.12"));
        assertEquals(new BigDecimal("1.10"), out);
    }

    @Test
    public void roundToFiveRappen_roundsUpWhenCloserToUpperStep() {
        // 1.13 is closer to 1.15 than to 1.10
        BigDecimal out = MoneyRounder.roundToFiveRappen(new BigDecimal("1.13"));
        assertEquals(new BigDecimal("1.15"), out);
    }

    @Test
    public void isNegative_trueOnlyForNegativeNumbers() {
        assertTrue(MoneyRounder.isNegative(new BigDecimal("-0.01")));
        assertFalse(MoneyRounder.isNegative(new BigDecimal("0.00")));
        assertFalse(MoneyRounder.isNegative(new BigDecimal("1.00")));
        assertFalse(MoneyRounder.isNegative(null));
    }
}
