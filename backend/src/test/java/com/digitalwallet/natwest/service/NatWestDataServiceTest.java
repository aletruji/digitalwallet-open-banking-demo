package com.oepfelbaum.digitalwallet.natwest.service;



import com.oepfelbaum.digitalwallet.natwest.client.NatWestClient;
import com.oepfelbaum.digitalwallet.natwest.config.NatWestProperties;
import com.oepfelbaum.digitalwallet.natwest.dto.NatWestAccountsResponse;
import com.oepfelbaum.digitalwallet.natwest.dto.NatWestBalancesResponse;
import com.oepfelbaum.digitalwallet.natwest.dto.NatWestTransactionsResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NatWestDataServiceTest {

    @Test
    void accounts_callsClientWithTokenUrlAndHeaders() {
        var client = mock(NatWestClient.class);
        var auth = mock(NatWestAuthService.class);

        when(auth.getValidUserAccessToken()).thenReturn("tok");

        when(client.props()).thenReturn(new NatWestProperties(
                "https://api",
                "https://ob",
                "cid",
                "secret",
                "https://redirect",
                "user"
        ));

        when(client.get(anyString(), anyString(), anyMap(), eq(NatWestAccountsResponse.class)))
                .thenReturn(null);

        var svc = new NatWestDataService(client, auth, () -> "fixed-id");

        svc.accounts();

        verify(auth, times(1)).getValidUserAccessToken();
        verify(client, times(1)).props();
        verify(client, times(1)).get(
                eq("https://ob/open-banking/v4.0/aisp/accounts"),
                eq("tok"),
                argThat(h ->
                        "application/json".equals(h.get("Accept")) &&
                                "0015800000jfwxXAAQ".equals(h.get("x-fapi-financial-id")) &&
                                "fixed-id".equals(h.get("x-fapi-interaction-id"))
                ),
                eq(NatWestAccountsResponse.class)
        );

        verifyNoMoreInteractions(auth, client);
    }

    @Test
    void balances_buildsCorrectUrl() {
        var client = mock(NatWestClient.class);
        var auth = mock(NatWestAuthService.class);

        when(auth.getValidUserAccessToken()).thenReturn("tok");
        when(client.props()).thenReturn(new NatWestProperties(
                "https://api",
                "https://ob",
                "cid",
                "secret",
                "https://redirect",
                "user"
        ));

        when(client.get(anyString(), anyString(), anyMap(), eq(NatWestBalancesResponse.class)))
                .thenReturn(null);

        var svc = new NatWestDataService(client, auth, () -> "id123");

        svc.balances("acc1");

        verify(client).get(
                eq("https://ob/open-banking/v4.0/aisp/accounts/acc1/balances"),
                eq("tok"),
                anyMap(),
                eq(NatWestBalancesResponse.class)
        );
    }

    @Test
    void transactions_buildsCorrectUrl() {
        var client = mock(NatWestClient.class);
        var auth = mock(NatWestAuthService.class);

        when(auth.getValidUserAccessToken()).thenReturn("tok");
        when(client.props()).thenReturn(new NatWestProperties(
                "https://api",
                "https://ob",
                "cid",
                "secret",
                "https://redirect",
                "user"
        ));

        when(client.get(anyString(), anyString(), anyMap(), eq(NatWestTransactionsResponse.class)))
                .thenReturn(null);

        var svc = new NatWestDataService(client, auth, () -> "id123");

        svc.transactions("acc9");

        verify(client).get(
                eq("https://ob/open-banking/v4.0/aisp/accounts/acc9/transactions"),
                eq("tok"),
                anyMap(),
                eq(NatWestTransactionsResponse.class)
        );
    }



}
