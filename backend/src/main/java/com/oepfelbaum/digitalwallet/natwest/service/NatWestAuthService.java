package com.oepfelbaum.digitalwallet.natwest.service;

import com.oepfelbaum.digitalwallet.natwest.client.NatWestClient;
import com.oepfelbaum.digitalwallet.natwest.db.NatWestTokenEntity;
import com.oepfelbaum.digitalwallet.natwest.db.NatWestTokenRepository;
import com.oepfelbaum.digitalwallet.natwest.dto.NatWestAuthorizeResponse;
import com.oepfelbaum.digitalwallet.natwest.dto.NatWestCreateConsentResponse;
import com.oepfelbaum.digitalwallet.natwest.dto.NatWestTokenResponse;
import com.oepfelbaum.digitalwallet.natwest.mapper.NatWestMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.Map;

@Service
public class NatWestAuthService {

    //change in real environment to multi-user setup
    private static final String TOKEN_ID = "demoUser";
    private static final Logger log = LoggerFactory.getLogger(NatWestAuthService.class);


    private final NatWestClient client;
    private final NatWestTokenRepository repo;
    private final NatWestMapper map;


    public NatWestAuthService(NatWestClient client, NatWestTokenRepository repo, NatWestMapper map) {
        this.client = client;
        this.repo = repo;
        this.map = map;
    }

    public void connect() {
        log.info("NatWest connect started for userId={}", TOKEN_ID);

        String appToken = getClientCredentialsToken();
        String consentId = createConsent(appToken);
        String code = authorizeAutoPostman(consentId);

        NatWestTokenResponse tokens = exchangeCodeForToken(code);
        storeTokens(tokens);

    }


    public void disconnect() {
        repo.deleteById(TOKEN_ID);
        log.info("NatWest disconnected for userId={}", TOKEN_ID);
    }


    public boolean isConnected() {
        return repo.findById(TOKEN_ID)
                .map(t -> t.getAccessToken() != null && !t.getAccessToken().isBlank())
                .orElse(false);
    }


         //1. send costumer id and secret, get acces_token for app auth
    private String getClientCredentialsToken() {
        String url = client.props().baseOb() + "/token";

        NatWestTokenResponse json = client.postForm(url, Map.of(
                "grant_type", "client_credentials",
                "client_id", client.props().clientId(),
                "client_secret", client.props().clientSecret(),
                "scope", "accounts"
        ), NatWestTokenResponse.class);

        if (json.accessToken() == null || json.accessToken().isBlank()) {
            throw new IllegalStateException("No access_token in client credentials response");
        }

        return json.accessToken();
    }


    //2. send permission request and header with access token, get consentId
    private String createConsent(String appToken) {
        String url = client.props().baseOb() + "/open-banking/v4.0/aisp/account-access-consents";

        Map<String, Object> payload = Map.of(
                "Data", Map.of("Permissions", new String[]{
                        "ReadAccountsDetail",
                        "ReadBalances",
                        "ReadTransactionsCredits",
                        "ReadTransactionsDebits",
                        "ReadTransactionsDetail"
                }),
                "Risk", Map.of()
        );

        NatWestCreateConsentResponse json =
                client.postJson(url, payload, appToken, NatWestCreateConsentResponse.class);

        if (json == null || json.Data() == null || json.Data().ConsentId() == null || json.Data().ConsentId().isBlank()) {
            throw new IllegalStateException("Consent response invalid (missing consentId)");
        }

        return json.Data().ConsentId();
    }

    //3. simulating user identification with auto postman sending consentId, getting authorization code
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
                .build(false)
                .toUriString();

        NatWestAuthorizeResponse json = client.get(
                url,
                null,
                Map.of("Accept", "application/json"),
                NatWestAuthorizeResponse.class
        );

        if (json == null || json.redirectUri() == null || json.redirectUri().isBlank()) {
            throw new IllegalStateException("Authorize response invalid (missing redirectUri)");}

        return map.extractCode(json.redirectUri());
    }

    //with clientId + secret + authorization code, getting access token (limited in time) and refresh token
    private NatWestTokenResponse exchangeCodeForToken(String code) {
        String url = client.props().baseOb() + "/token";
        return client.postForm(url, Map.of(
                "client_id", client.props().clientId(),
                "client_secret", client.props().clientSecret(),
                "redirect_uri", client.props().redirectUri(),
                "grant_type", "authorization_code",
                "code", code
        ), NatWestTokenResponse.class);
    }


    private void storeTokens(NatWestTokenResponse json) {
        String access = json.accessToken();
        String refresh = json.refreshToken();
        Long expiresIn = json.expiresIn();

        if (access == null || access.isBlank() || expiresIn == null) {
            throw new IllegalStateException("Token response invalid (missing access_token/expires_in)");
        }

        Instant expiresAt = Instant.now().plusSeconds(Math.max(0, expiresIn - 10));

        NatWestTokenEntity e = repo.findById(TOKEN_ID)
                .orElseGet(() -> new NatWestTokenEntity(TOKEN_ID, access, expiresAt, refresh));

        e.setAccessToken(access);
        e.setAccessTokenExpiresAt(expiresAt);

        if (refresh != null && !refresh.isBlank()) {
            e.setRefreshToken(refresh);
        }

        repo.save(e);
    }


    private NatWestTokenResponse refreshAccessToken(String refreshToken) {
        String url = client.props().baseOb() + "/token";
        return client.postForm(url, Map.of(
                "grant_type", "refresh_token",
                "refresh_token", refreshToken,
                "client_id", client.props().clientId(),
                "client_secret", client.props().clientSecret()
        ), NatWestTokenResponse.class);
    }


    public String getValidUserAccessToken() {
        log.info("NatWest access token expired. Refreshing token");

        NatWestTokenEntity e = repo.findById(TOKEN_ID)
                .orElseThrow(() -> new IllegalStateException("Not connected. Call POST /api/natwest/connect"));

        if (e.getAccessToken() != null && e.getAccessTokenExpiresAt() != null && Instant.now().isBefore(e.getAccessTokenExpiresAt())) {
            return e.getAccessToken();
        }

        if (e.getRefreshToken() == null || e.getRefreshToken().isBlank()) {
            log.warn("NatWest refresh token missing for userId={}", TOKEN_ID);
            throw new IllegalStateException("No refresh token. Reconnect needed.");
        }

        NatWestTokenResponse refreshed = refreshAccessToken(e.getRefreshToken());
        storeTokens(refreshed);

        return refreshed.accessToken();
    }

  }
