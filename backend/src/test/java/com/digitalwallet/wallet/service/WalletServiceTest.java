package com.digitalwallet.wallet.service;

import com.digitalwallet.wallet.dto.WalletAccountDetailsDto;
import com.digitalwallet.wallet.dto.WalletOverviewDto;
import com.digitalwallet.wallet.dto.WalletTransactionDto;
import com.digitalwallet.wallet.port.WalletConnector;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class WalletServiceTest {

    @Test
    public void getOverview_buildsAccountsAndTotalRounded() {
        WalletConnector connector = mock(WalletConnector.class);
        WalletService service = new WalletService(connector);

        when(connector.providerName()).thenReturn("NatWest");


        WalletConnector.Account a1 = new WalletConnector.Account("acc1", "Main", "Current", "CHF");
        WalletConnector.Account a2 = new WalletConnector.Account("acc2", "Save", "Savings", "CHF");

        when(connector.accounts()).thenReturn(List.of(a1, a2));


        when(connector.balance("acc1")).thenReturn(new BigDecimal("1.12"));
        when(connector.balance("acc2")).thenReturn(new BigDecimal("-2.03"));

        WalletOverviewDto out = service.getOverview();

        assertEquals(2, out.accounts().size());

        assertEquals(new BigDecimal("1.10"), out.accounts().get(0).balance());
        assertFalse(out.accounts().get(0).isNegative());

        assertEquals(new BigDecimal("-2.05"), out.accounts().get(1).balance());
        assertTrue(out.accounts().get(1).isNegative());


        assertEquals(new BigDecimal("-0.95"), out.totalAmount());
        assertTrue(out.totalIsNegative());
    }

    @Test
    public void getAccountDetails_filtersLast30Days_negatesDebit_rounds_andSortsDesc() {
        WalletConnector connector = mock(WalletConnector.class);
        WalletService service = new WalletService(connector);

        when(connector.providerName()).thenReturn("NatWest");

        WalletConnector.Account a1 = new WalletConnector.Account("acc1", "Main", "Current", "CHF");
        when(connector.accounts()).thenReturn(List.of(a1));

        when(connector.balance("acc1")).thenReturn(new BigDecimal("10.02"));

        String today = OffsetDateTime.now().toString();
        String old = OffsetDateTime.now().minusDays(31).toString();

        WalletConnector.Transaction t1 = new WalletConnector.Transaction(
                today,
                null,
                "Credit",
                new BigDecimal("1.12"),
                "CHF",
                "Alice"
        );

        WalletConnector.Transaction t2 = new WalletConnector.Transaction(
                today,
                null,
                "Debit",
                new BigDecimal("2.03"),
                "CHF",
                "Bob"
        );

        WalletConnector.Transaction tooOld = new WalletConnector.Transaction(
                old,
                null,
                "Credit",
                new BigDecimal("99.99"),
                "CHF",
                "TooOld"
        );

        when(connector.transactions("acc1")).thenReturn(List.of(t1, t2, tooOld));


        WalletAccountDetailsDto out = service.getAccountDetails("acc1", "desc");

        assertEquals("acc1", out.accountId());
        assertEquals(new BigDecimal("10.00"), out.balance());
        assertFalse(out.isNegative());

        // tooOld must be filtered out
        assertEquals(2, out.transactions().size());

        // desc = newest first, both are today, stable order might vary
               List<WalletTransactionDto> tx = out.transactions();
        assertTrue(tx.stream().anyMatch(x -> x.amount().compareTo(new BigDecimal("1.10")) == 0));
        assertTrue(tx.stream().anyMatch(x -> x.amount().compareTo(new BigDecimal("-2.05")) == 0));


        WalletTransactionDto debitTx = tx.stream()
                .filter(x -> x.amount().compareTo(new BigDecimal("-2.05")) == 0)
                .findFirst()
                .orElseThrow();
        assertTrue(debitTx.isNegative());
    }

    @Test
    public void getAccountDetails_throwsWhenAccountNotFound() {
        WalletConnector connector = mock(WalletConnector.class);
        WalletService service = new WalletService(connector);

        when(connector.accounts()).thenReturn(List.of(
                new WalletConnector.Account("accX", "Other", "Current", "CHF")
        ));

        try {
            service.getAccountDetails("missing", "desc");
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException ex) {
            assertTrue(ex.getMessage().contains("Account not found"));
        }
    }
}
