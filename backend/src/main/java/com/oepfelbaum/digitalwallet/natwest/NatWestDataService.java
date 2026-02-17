package com.oepfelbaum.digitalwallet.natwest;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class NatWestDataService {

    private final NatWestClient client;
    private final NatWestAuthService auth;

    public NatWestDataService(NatWestClient client, NatWestAuthService auth) {
        this.client = client;
        this.auth = auth;
    }

    public Map<String, Object> accounts() {
        String token = auth.getValidUserAccessToken();
        String url = client.props().baseOb() + "/open-banking/v4.0/aisp/accounts";

        return client.get(url, token, Map.of(
                "Accept", "application/json",
                "x-fapi-financial-id", "0015800000jfwxXAAQ",
                "x-fapi-interaction-id", UUID.randomUUID().toString()
        ));
    }

    public Map<String, Object> balances(String accountId) {
        String token = auth.getValidUserAccessToken();
        String url = client.props().baseOb() + "/open-banking/v4.0/aisp/accounts/" + accountId + "/balances";

        return client.get(url, token, Map.of(
                "Accept", "application/json",
                "x-fapi-financial-id", "0015800000jfwxXAAQ",
                "x-fapi-interaction-id", UUID.randomUUID().toString()
        ));
    }

    public Map<String, Object> transactions(String accountId) {
        String token = auth.getValidUserAccessToken();
        String url = client.props().baseOb() + "/open-banking/v4.0/aisp/accounts/" + accountId + "/transactions";

        return client.get(url, token, Map.of(
                "Accept", "application/json",
                "x-fapi-financial-id", "0015800000jfwxXAAQ",
                "x-fapi-interaction-id", UUID.randomUUID().toString()
        ));
    }
}
