package com.oepfelbaum.digitalwallet.natwest;

import com.oepfelbaum.digitalwallet.natwest.db.NatWestTokenEntity;
import com.oepfelbaum.digitalwallet.natwest.db.NatWestTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.Map;


@Service
public class NatWestAuthService {

    private static final String TOKEN_ID = "demoUser";

    private final NatWestClient client;
    private final NatWestTokenRepository repo;

    public NatWestAuthService(NatWestClient client, NatWestTokenRepository repo) {
        this.client = client;
        this.repo = repo;
    }

    public Map<String, Object> connect() {



        String appToken = getClientCredentialsToken();
        String consentId = createConsent(appToken);
        String code = authorizeAutoPostman(consentId);
        Map<String, Object> tokens = exchangeCodeForToken(code);
        storeTokens(tokens);
        return Map.of("status", "connected", "consentId", consentId);
    }

    public String getValidUserAccessToken() {
        NatWestTokenEntity e = repo.findById(TOKEN_ID)
                .orElseThrow(() -> new IllegalStateException("Not connected. Call POST /api/natwest/connect"));

        if (e.getAccessToken() != null && e.getAccessTokenExpiresAt() != null && Instant.now().isBefore(e.getAccessTokenExpiresAt())) {
            return e.getAccessToken();
        }

        if (e.getRefreshToken() == null || e.getRefreshToken().isBlank()) {
            throw new IllegalStateException("No refresh token. Reconnect needed.");
        }

        Map<String, Object> refreshed = refreshAccessToken(e.getRefreshToken());
        storeTokens(refreshed);
        return repo.findById(TOKEN_ID).orElseThrow().getAccessToken();
    }

    private void storeTokens(Map<String, Object> json) {
        String access = (String) json.get("access_token");
        String refresh = (String) json.get("refresh_token");
        Number expiresIn = (Number) json.get("expires_in");

        Instant expiresAt = Instant.now().plusSeconds(expiresIn.longValue() - 10);

        NatWestTokenEntity e = repo.findById(TOKEN_ID)
                .orElseGet(() -> new NatWestTokenEntity(TOKEN_ID, access, expiresAt, refresh));

        e.setAccessToken(access);
        e.setAccessTokenExpiresAt(expiresAt);

        if (refresh != null && !refresh.isBlank()) e.setRefreshToken(refresh);

        repo.save(e);
    }

    private String getClientCredentialsToken() {
        String url = client.props().baseOb() + "/token";
        Map<String, Object> json = client.postForm(url, Map.of(
                "grant_type", "client_credentials",
                "client_id", client.props().clientId(),
                "client_secret", client.props().clientSecret(),
                "scope", "accounts"
        ));
        return (String) json.get("access_token");
    }

    private String createConsent(String appToken) {
        String url = client.props().baseOb() + "/open-banking/v4.0/aisp/account-access-consents";

        Map<String, Object> payload = Map.of(
                "Data", Map.of("Permissions", new String[] {
                        "ReadAccountsDetail",
                        "ReadBalances",
                        "ReadTransactionsCredits",
                        "ReadTransactionsDebits",
                        "ReadTransactionsDetail"
                }),
                "Risk", Map.of()
        );

        Map<String, Object> json = client.postJson(url, payload, appToken);
        Map<String, Object> data = (Map<String, Object>) json.get("Data");
        return (String) data.get("ConsentId");
    }



    private String authorizeAutoPostman(String consentId) {
        var p = client.props();

        String url = UriComponentsBuilder
                .fromUriString(p.baseApi() + "/authorize")
                .queryParam("client_id", p.clientId())
                .queryParam("response_type", "code id_token")
                .queryParam("scope", "openid accounts")
                .queryParam("redirect_uri", p.redirectUri())
                .queryParam("request", consentId)
                .queryParam("authorization_mode", "AUTO_POSTMAN")
                .queryParam("authorization_result", "APPROVED")
                .queryParam("authorization_username", p.psuUsername())
                .queryParam("authorization_accounts", "*")
                .build(false)          // wichtig: true = korrekt encoding
                .toUriString();



        // WICHTIG: Accept JSON, sonst kann NatWest auch anders reagieren
        var json = client.get(url, null, Map.of(
                "Accept", "application/json"
        ));

        String redirect = (String) json.get("redirectUri");

        return extractCode(redirect);
    }

    private String extractCode(String redirectUrl) {
        if (redirectUrl == null || redirectUrl.isBlank()) {
            throw new IllegalStateException("redirectUrl ist leer");
        }

        var uri = UriComponentsBuilder.fromUriString(redirectUrl).build(true);
        String code = uri.getQueryParams().getFirst("code");
        if (code != null && !code.isBlank()) {
            return code;
        }

        String fragment = uri.getFragment();
        if (fragment != null && fragment.contains("code=")) {
            String after = fragment.substring(fragment.indexOf("code=") + 5);
            int amp = after.indexOf('&');
            return amp >= 0 ? after.substring(0, amp) : after;
        }

        throw new IllegalStateException("Kein code in redirectUrl gefunden: " + redirectUrl);
    }


    private Map<String, Object> exchangeCodeForToken(String code) {
        String url = client.props().baseOb() + "/token";
        return client.postForm(url, Map.of(
                "client_id", client.props().clientId(),
                "client_secret", client.props().clientSecret(),
                "redirect_uri", client.props().redirectUri(),
                "grant_type", "authorization_code",
                "code", code
        ));
    }

    private Map<String, Object> refreshAccessToken(String refreshToken) {
        String url = client.props().baseOb() + "/token";
        return client.postForm(url, Map.of(
                "grant_type", "refresh_token",
                "refresh_token", refreshToken,
                "client_id", client.props().clientId(),
                "client_secret", client.props().clientSecret()
        ));
    }


}
