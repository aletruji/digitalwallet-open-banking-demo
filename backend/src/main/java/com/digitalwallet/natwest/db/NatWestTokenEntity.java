package com.oepfelbaum.digitalwallet.natwest.db;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

import java.time.Instant;

@Entity
public class NatWestTokenEntity {

    @Id
    private String id;

    @Lob
        private String accessToken;


    private Instant accessTokenExpiresAt;

    @Lob
        private String refreshToken;

    protected NatWestTokenEntity() {}

    public NatWestTokenEntity(String id, String accessToken, Instant accessTokenExpiresAt, String refreshToken) {
        this.id = id;
        this.accessToken = accessToken;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
        this.refreshToken = refreshToken;
    }

    public String getId() { return id; }
    public String getAccessToken() { return accessToken; }
    public Instant getAccessTokenExpiresAt() { return accessTokenExpiresAt; }
    public String getRefreshToken() { return refreshToken; }

    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public void setAccessTokenExpiresAt(Instant accessTokenExpiresAt) { this.accessTokenExpiresAt = accessTokenExpiresAt; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
