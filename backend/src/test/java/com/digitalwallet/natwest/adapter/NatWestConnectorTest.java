package com.oepfelbaum.digitalwallet.natwest.adapter;

import com.oepfelbaum.digitalwallet.natwest.dto.NatWestAccountsResponse;
import com.oepfelbaum.digitalwallet.natwest.service.NatWestAuthService;
import com.oepfelbaum.digitalwallet.natwest.service.NatWestDataService;
import com.oepfelbaum.digitalwallet.wallet.port.WalletConnector;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class NatWestConnectorTest {

    @Test
    public void accounts_mapsFields_andAppliesDefaults() {
        NatWestAuthService auth = mock(NatWestAuthService.class);
        NatWestDataService data = mock(NatWestDataService.class);




        NatWestAccountsResponse.Account a1 = new NatWestAccountsResponse.Account(
                "id1",
                "EUR",
                "CACC",
                null,
                "Main"
        );

        NatWestAccountsResponse.Account a2 = new NatWestAccountsResponse.Account(
                "id2",
                null,
                "SVGS",
                null,
                "   "
        );


        NatWestAccountsResponse res = new NatWestAccountsResponse(
                new NatWestAccountsResponse.Data(List.of(a1, a2))
        );

        when(data.accounts()).thenReturn(res);

        NatWestConnector connector = new NatWestConnector(auth, data);

        List<WalletConnector.Account> out = connector.accounts();

        assertEquals(2, out.size());


        assertEquals(new WalletConnector.Account("id1", "Main", "Current", "EUR"), out.get(0));
        assertEquals(new WalletConnector.Account("id2", "Account", "Savings", "GBP"), out.get(1));
    }

    @Test
    public void balance_returnsZeroWhenNoBalancesResponse() {
        NatWestAuthService auth = mock(NatWestAuthService.class);
        NatWestDataService data = mock(NatWestDataService.class);

        when(data.balances("acc1")).thenReturn(null);

        NatWestConnector connector = new NatWestConnector(auth, data);

        BigDecimal out = connector.balance("acc1");

        assertEquals(BigDecimal.ZERO, out);
    }
}
