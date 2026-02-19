package com.oepfelbaum.digitalwallet.natwest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NatWestAuthorizeResponse(String redirectUri) {}
