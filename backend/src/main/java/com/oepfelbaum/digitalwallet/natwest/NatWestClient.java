package com.oepfelbaum.digitalwallet.natwest;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Component
public class NatWestClient {

    private final RestClient rest;
    private final NatWestProperties props;

    public NatWestClient(NatWestProperties props) {
        this.rest = RestClient.create();
        this.props = props;
    }

    public Map<String, Object> postForm(String url, Map<String, String> form) {
        String body = form.entrySet().stream()
                .map(e -> enc(e.getKey()) + "=" + enc(e.getValue()))
                .reduce((a, b) -> a + "&" + b)
                .orElse("");

        return rest.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(Map.class);
    }

    public Map<String, Object> postJson(String url, Object json, String bearerToken) {
        var req = rest.post().uri(url).contentType(MediaType.APPLICATION_JSON).body(json);
        if (bearerToken != null) req = req.header("Authorization", "Bearer " + bearerToken);
        return req.retrieve().body(Map.class);
    }



    public Map<String, Object> get(String url, String bearerToken) {
        return get(url, bearerToken, Map.of(
                "Accept", "application/json",
                "x-fapi-financial-id", "0015800000jfwxXAAQ",
                "x-fapi-interaction-id", java.util.UUID.randomUUID().toString()
        ));
    }


    public Map<String, Object> get(String url, String bearerToken, Map<String, String> headers) {
        var req = rest.get().uri(url);

        if (bearerToken != null && !bearerToken.isBlank()) {
            req = req.header("Authorization", "Bearer " + bearerToken);
        }

        if (headers != null) {
            for (var e : headers.entrySet()) {
                req = req.header(e.getKey(), e.getValue());
            }
        }

        return req.retrieve().body(Map.class);
    }




    public NatWestProperties props() { return props; }

    public static String enc(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }

}
