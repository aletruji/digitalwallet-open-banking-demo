package com.oepfelbaum.digitalwallet.natwest;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "natwest")
public record NatWestProperties(
        String baseApi,
        String baseOb,
        String clientId,
        String clientSecret,
        String redirectUri,
        String psuUsername
) {}