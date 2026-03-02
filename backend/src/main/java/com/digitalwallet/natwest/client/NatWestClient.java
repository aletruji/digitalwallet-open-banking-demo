package com.oepfelbaum.digitalwallet.natwest.client;

import com.oepfelbaum.digitalwallet.natwest.config.NatWestProperties;
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


    public <T> T postForm(String url, Map<String,String> form, Class<T> type)
    {
        String body = form.entrySet().stream()
                .map(e -> enc(e.getKey()) + "=" + enc(e.getValue()))
                .reduce((a, b) -> a + "&" + b)
                .orElse("");

        return rest.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(type);
    }


    public <T> T postJson(String url, Object json, String bearerToken, Class<T> type) {
        var req = rest.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
        if (bearerToken != null && !bearerToken.isBlank()) {
            req = req.header("Authorization", "Bearer " + bearerToken);
        }
        return req.retrieve().body(type);
    }


    public <T> T get(String url, String bearerToken, Map<String, String> headers, Class<T> type) {
        var req = rest.get().uri(url);

        if (bearerToken != null && !bearerToken.isBlank()) {
            req = req.header("Authorization", "Bearer " + bearerToken);
        }

        if (headers != null) {
            for (var e : headers.entrySet()) {
                req = req.header(e.getKey(), e.getValue());
            }
        }

        return req.retrieve().body(type);
    }


    public NatWestProperties props() { return props; }


    private static String enc(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }

}
