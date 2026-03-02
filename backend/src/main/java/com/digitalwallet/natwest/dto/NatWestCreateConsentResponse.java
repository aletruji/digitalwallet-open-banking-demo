package com.oepfelbaum.digitalwallet.natwest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NatWestCreateConsentResponse(Data Data) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(String ConsentId) {}
}
