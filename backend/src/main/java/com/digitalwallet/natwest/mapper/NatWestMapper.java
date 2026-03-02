package com.digitalwallet.natwest.mapper;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
public class NatWestMapper {

    public String extractCode(String redirectUrl) {
        if (redirectUrl == null || redirectUrl.isBlank()) {
            throw new IllegalStateException("redirectUrl is empty");
        }

        var uri = UriComponentsBuilder.fromUriString(redirectUrl).build(true);

        String code = uri.getQueryParams().getFirst("code");
        if (code != null && !code.isBlank()) return code;

        String fragment = uri.getFragment();
        if (fragment != null && fragment.contains("code=")) {
            String after = fragment.substring(fragment.indexOf("code=") + 5);
            int amp = after.indexOf('&');
            return amp >= 0 ? after.substring(0, amp) : after;
        }

        throw new IllegalStateException("no code found in redirectUrl: " + redirectUrl);
    }

    
}
