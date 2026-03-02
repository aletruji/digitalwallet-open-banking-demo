package com.digitalwallet.natwest.service;

import com.digitalwallet.natwest.client.NatWestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.digitalwallet.natwest.dto.NatWestAccountsResponse;
import com.digitalwallet.natwest.dto.NatWestBalancesResponse;
import com.digitalwallet.natwest.dto.NatWestTransactionsResponse;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;


@Service
public class NatWestDataService {

    private final NatWestClient client;
    private final NatWestAuthService auth;
    private final Supplier<String> interactionId;

    @Autowired
    public NatWestDataService(NatWestClient client, NatWestAuthService auth) {
        this(client, auth, () -> UUID.randomUUID().toString());
    }

    public NatWestDataService(NatWestClient client, NatWestAuthService auth, Supplier<String> interactionId) {
        this.client = client;
        this.auth = auth;
        this.interactionId = interactionId;
    }

    private Map<String, String> headers() {
        return Map.of(
                "Accept", "application/json",
                "x-fapi-financial-id", "0015800000jfwxXAAQ",
                "x-fapi-interaction-id", interactionId.get()
        );
    }

    public NatWestAccountsResponse accounts() {
        String token = auth.getValidUserAccessToken();
        String url = client.props().baseOb() + "/open-banking/v4.0/aisp/accounts";

        return client.get(url, token, headers(), NatWestAccountsResponse.class);
    }


    public NatWestBalancesResponse balances(String accountId) {
        String token = auth.getValidUserAccessToken();
        String url = client.props().baseOb() + "/open-banking/v4.0/aisp/accounts/" + accountId + "/balances";

        return client.get(url, token, headers(), NatWestBalancesResponse.class);
    }


    public NatWestTransactionsResponse transactions(String accountId) {
        String token = auth.getValidUserAccessToken();
        String url = client.props().baseOb() + "/open-banking/v4.0/aisp/accounts/" + accountId + "/transactions";

        return client.get(url, token, headers(), NatWestTransactionsResponse.class);
    }

}
